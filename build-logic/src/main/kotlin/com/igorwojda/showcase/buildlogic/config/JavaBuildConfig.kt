package com.uzuu.diuchat.buildlogic.config

import org.gradle.api.JavaVersion
import java.io.File

object JavaBuildConfig {
    /**
     * Reads the Java version from the `gradle/libs.versions.toml` file.
     * (VersionCatalogsExtension is not available at this stage).
     */
    private val tomlJavaVersion by lazy {
        // ponytail: resolve from working directory ancestors; use Gradle's version catalog if builds run outside this tree.
        val catalog = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .map { it.resolve("gradle/libs.versions.toml") }
            .firstOrNull { it.isFile }
            ?: error("Could not find gradle/libs.versions.toml from ${System.getProperty("user.dir")}")

        catalog.readLines()
            .firstOrNull { it.substringBefore("=").trim() == "java" }
            ?.substringAfter("=")
            ?.trim('"', ' ')
            ?: error("❌ Could not find 'java' version in libs.versions.toml file")
    }

    /*
    Configure the buildLogic config to target JDK from version catalog
    This matches the JDK used to build the project.
     */
    val JAVA_VERSION: JavaVersion = JavaVersion.toVersion(tomlJavaVersion)
    val JVM_TOOLCHAIN_VERSION: Int = tomlJavaVersion.toInt()
}
