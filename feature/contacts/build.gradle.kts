plugins {
    id("com.uzuu.diuchat.convention.feature")
}

android {
    namespace = "com.uzuu.diuchat.feature.contacts"
}

dependencies {
    implementation(platform(libs.supabase.bom))
    implementation(libs.bundles.supabase)
    implementation(libs.bundles.camerax)
    implementation(libs.zxing.core)
}
