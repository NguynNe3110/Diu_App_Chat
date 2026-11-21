plugins {
    id("com.uzuu.diuchat.convention.feature")
}

android {
    namespace = "com.uzuu.diuchat.feature.home"
}

dependencies {
    implementation(projects.feature.base)
    implementation(platform(libs.supabase.bom))
    implementation(libs.bundles.supabase)
}
