plugins {
    id("com.uzuu.diuchat.convention.feature")
}

android {
    namespace = "com.uzuu.diuchat.feature.media"
}

dependencies {
    implementation(projects.feature.base)
}
