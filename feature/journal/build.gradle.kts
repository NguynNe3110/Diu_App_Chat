plugins {
    id("com.uzuu.diuchat.convention.feature")
}

android {
    namespace = "com.uzuu.diuchat.feature.journal"
}

dependencies {
    implementation(projects.feature.base)
    implementation(platform(libs.supabase.bom))
    implementation(libs.bundles.supabase)
}
