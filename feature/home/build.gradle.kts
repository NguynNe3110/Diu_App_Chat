plugins {
    id("com.uzuu.diuchat.convention.feature")
}

android {
    namespace = "com.uzuu.diuchat.feature.home"
}

dependencies {
    implementation(projects.feature.base)
}
