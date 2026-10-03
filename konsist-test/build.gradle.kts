plugins {
    id("com.uzuu.diuchat.convention.test.library")
}

android {
    namespace = "com.uzuu.diuchat.konsist.test"
}

dependencies {
    implementation(projects.feature.base)

    testImplementation(projects.library.testUtils)
    testImplementation(libs.bundles.test)
    testImplementation(libs.konsist)
    testImplementation(libs.viewmodel.ktx)
}
