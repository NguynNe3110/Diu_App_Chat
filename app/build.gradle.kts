import com.uzuu.diuchat.buildlogic.ext.buildConfigFieldFromGradleProperty

plugins {
    id("com.uzuu.diuchat.convention.application")
}

android {
    namespace = "com.uzuu.diuchat.app"

    defaultConfig {
        applicationId = "com.uzuu.diuchat"

        versionCode = 1
        versionName = "0.0.1" // SemVer (Major.Minor.Patch)

        buildConfigFieldFromGradleProperty(project, "apiBaseUrl")
        buildConfigFieldFromGradleProperty(project, "apiToken")
        //supabase
        buildConfigFieldFromGradleProperty(project, "supabaseUrl")
        buildConfigFieldFromGradleProperty(project, "supabaseAnonKey")
    }

    buildTypes {
        getByName("release") {
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
    implementation(projects.feature.album)
    implementation(projects.feature.onboarding)

}
