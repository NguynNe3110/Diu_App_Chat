package com.uzuu.diuchat.buildlogic

import com.uzuu.diuchat.buildlogic.config.JavaBuildConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

/**
 * Kotlin compilation is provided by AGP built-in Kotlin support (AGP 9+).
 */
class KotlinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            kotlinExtension.jvmToolchain(JavaBuildConfig.JVM_TOOLCHAIN_VERSION)
        }
    }
}
