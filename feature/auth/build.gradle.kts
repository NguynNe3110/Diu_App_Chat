import com.uzuu.diuchat.buildlogic.ext.buildConfigFieldFromGradleProperty

plugins {
    id("com.uzuu.diuchat.convention.feature")
}

android {
    namespace = "com.uzuu.diuchat.feature.auth"

    defaultConfig {
        buildConfigFieldFromGradleProperty(project, "googleWebClientId")
    }
}

dependencies {
    implementation(platform(libs.supabase.bom))
    implementation(libs.bundles.supabase)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
}
