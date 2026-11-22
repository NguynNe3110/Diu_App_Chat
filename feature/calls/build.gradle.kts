import com.uzuu.diuchat.buildlogic.ext.buildConfigFieldFromGradleProperty

plugins {
    id("com.uzuu.diuchat.convention.feature")
}

android {
    namespace = "com.uzuu.diuchat.feature.calls"

    defaultConfig {
        buildConfigFieldFromGradleProperty(project, "supabaseUrl")
        buildConfigFieldFromGradleProperty(project, "supabaseAnonKey")
    }
}

dependencies {
    implementation(platform(libs.supabase.bom))
    implementation(libs.bundles.supabase)
    implementation(libs.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.livekit.android)
}
