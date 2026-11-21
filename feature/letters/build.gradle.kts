plugins {
    id("com.uzuu.diuchat.convention.feature")
}

android {
    namespace = "com.uzuu.diuchat.feature.letters"
}

dependencies {
    implementation(platform(libs.supabase.bom))
    implementation(libs.bundles.supabase)
}
