import com.uzuu.diuchat.buildlogic.ext.buildConfigFieldFromGradleProperty
import java.io.File
import java.io.StringReader
import java.util.Properties

plugins {
    id("com.uzuu.diuchat.convention.application")
    id("com.uzuu.diuchat.convention.test")
    alias(libs.plugins.google.services)
}

val keystoreProperties =
    Properties().apply {
        providers
            .fileContents(rootProject.layout.projectDirectory.file("keystore.properties"))
            .asText
            .orNull
            ?.let { load(StringReader(it)) }
    }

fun releaseSigningValue(
    propertyKey: String,
    envKey: String,
): String? = providers
    .environmentVariable(envKey)
    .orNull
    ?.takeIf { it.isNotBlank() }
    ?: keystoreProperties.getProperty(propertyKey)?.trim()?.takeIf { it.isNotEmpty() }

val releaseStoreFilePath = releaseSigningValue("storeFile", "ANDROID_KEYSTORE_PATH")
val releaseStorePassword = releaseSigningValue("storePassword", "ANDROID_KEYSTORE_PASSWORD")
val releaseKeyAlias = releaseSigningValue("keyAlias", "ANDROID_KEY_ALIAS")
val releaseKeyPassword = releaseSigningValue("keyPassword", "ANDROID_KEY_PASSWORD")
val hasReleaseSigning =
    listOf(
        releaseStoreFilePath,
        releaseStorePassword,
        releaseKeyAlias,
        releaseKeyPassword,
    ).all { it != null }

android {
    namespace = "com.uzuu.diuchat.app"

    defaultConfig {
        applicationId = "com.uzuu.diuchat"

        versionCode = 1
        versionName = "0.0.1" // SemVer (Major.Minor.Patch)

        buildConfigFieldFromGradleProperty(project, "apiBaseUrl")
        buildConfigFieldFromGradleProperty(project, "apiToken")
        // supabase
        buildConfigFieldFromGradleProperty(project, "supabaseUrl")
        buildConfigFieldFromGradleProperty(project, "supabaseAnonKey")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                val configuredPath = requireNotNull(releaseStoreFilePath)
                storeFile =
                    File(configuredPath).takeIf { it.isAbsolute }
                        ?: rootProject.layout.projectDirectory.file(configuredPath).asFile
                storePassword = requireNotNull(releaseStorePassword)
                keyAlias = requireNotNull(releaseKeyAlias)
                keyPassword = requireNotNull(releaseKeyPassword)
            }
        }
    }

    buildTypes {
        getByName("release") {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
            proguardFiles("proguard-android.txt", "proguard-rules.pro")
        }
    }
}

dependencies {
    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.bundles.supabase)
    implementation(libs.ktor.client.okhttp)

    // "projects." Syntax utilizes Gradle TYPESAFE_PROJECT_ACCESSORS feature
    implementation(projects.feature.base)
    implementation(projects.feature.onboarding)
    implementation(projects.feature.auth)
    implementation(projects.feature.contacts)
    implementation(projects.feature.home)
    implementation(projects.feature.chat)
    implementation(projects.feature.settings)
    implementation(projects.feature.media)
    implementation(projects.feature.letters)
    implementation(projects.feature.journal)
    implementation(projects.feature.calls)
    implementation(projects.feature.location)
    // Firebase Cloud Messaging
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    testImplementation(libs.bundles.test)
}
