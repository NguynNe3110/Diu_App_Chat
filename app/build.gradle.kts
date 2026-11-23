import com.uzuu.diuchat.buildlogic.ext.buildConfigFieldFromGradleProperty

plugins {
    id("com.uzuu.diuchat.convention.application")
    id("com.uzuu.diuchat.convention.test")
    alias(libs.plugins.google.services)
}

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

    buildTypes {
        getByName("release") {
            // ponytail: test artifacts only; use a stable release key for production.
            if (providers.gradleProperty("ciDebugSigning").orNull == "true") {
                signingConfig = signingConfigs.getByName("debug")
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
