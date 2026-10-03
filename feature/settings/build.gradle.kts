plugins {
    id("com.uzuu.diuchat.convention.feature")
}

android {
    namespace = "com.uzuu.diuchat.feature.settings"
}

dependencies {
    implementation(libs.aboutlibraries.compose)
}
