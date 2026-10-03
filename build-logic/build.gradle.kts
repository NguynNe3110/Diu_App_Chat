import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.uzuu.diuchat.buildlogic"

/*
Configure the build-logic plugins to target JDK from version catalog
This matches the JDK used to build the project, and is not related to what is running on device.
*/
val javaVersion =
    libs
        .versions
        .java
        .get()

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(javaVersion)
    }

    jvmToolchain(javaVersion.toInt())
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.spotless.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
    implementation(libs.test.logger.gradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.junit5.gradlePlugin)
    implementation(libs.easy.launcher.gradlePlugin)
    implementation(libs.about.libraries.gradlePlugin)

    /*
    Expose generated type-safe version catalogs accessors accessible from precompiled script plugins
    e.g. add("implementation", libs.koin)
    https://github.com/gradle/gradle/issues/15383
     */
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("applicationConvention") {
            id = "com.uzuu.diuchat.convention.application"
            implementationClass = "com.uzuu.diuchat.buildlogic.ApplicationConventionPlugin"
        }

        register("featureConvention") {
            id = "com.uzuu.diuchat.convention.feature"
            implementationClass = "com.uzuu.diuchat.buildlogic.FeatureConventionPlugin"
        }

        register("libraryConvention") {
            id = "com.uzuu.diuchat.convention.library"
            implementationClass = "com.uzuu.diuchat.buildlogic.LibraryConventionPlugin"
        }

        register("kotlinConvention") {
            id = "com.uzuu.diuchat.convention.kotlin"
            implementationClass = "com.uzuu.diuchat.buildlogic.KotlinConventionPlugin"
        }

        register("testConvention") {
            id = "com.uzuu.diuchat.convention.test"
            implementationClass = "com.uzuu.diuchat.buildlogic.TestConventionPlugin"
        }

        register("testLibraryConvention") {
            id = "com.uzuu.diuchat.convention.test.library"
            implementationClass = "com.uzuu.diuchat.buildlogic.TestConventionLibraryPlugin"
        }

        register("spotlessConvention") {
            id = "com.uzuu.diuchat.convention.spotless"
            implementationClass = "com.uzuu.diuchat.buildlogic.SpotlessConventionPlugin"
        }

        register("detektConvention") {
            id = "com.uzuu.diuchat.convention.detekt"
            implementationClass = "com.uzuu.diuchat.buildlogic.DetektConventionPlugin"
        }

        register("easyLauncherConvention") {
            id = "com.uzuu.diuchat.convention.easylauncher"
            implementationClass = "com.uzuu.diuchat.buildlogic.EasyLauncherConventionPlugin"
        }

        register("aboutLibrariesConvention") {
            id = "com.uzuu.diuchat.convention.aboutlibraries"
            implementationClass = "com.uzuu.diuchat.buildlogic.AboutLibrariesConventionPlugin"
        }
    }
}
