plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.uzuu.test"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.core.ktx)
    implementation(libs.material.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso)
    androidTestImplementation(libs.ext.junit)
}
