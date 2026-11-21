plugins {
    id("com.uzuu.diuchat.convention.feature")
}

android {
    namespace = "com.uzuu.diuchat.feature.base"
}

dependencies {
    implementation(libs.zxing.core)
}
