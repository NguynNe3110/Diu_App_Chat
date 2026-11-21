package com.uzuu.diuchat.buildlogic

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding
import com.uzuu.diuchat.buildlogic.ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

private const val RULE_DISABLED = "disabled"

class SpotlessConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.diffplug.spotless")

            extensions.configure<SpotlessExtension> {
                // Keep formatting identical on Windows worktrees and Linux CI runners.
                lineEndings = LineEnding.UNIX
                kotlin {
                    target("**/*.kt", "**/*.kts")

                    // Some rules are disabled in .editorconfig to avoid conflicts with detekt
                    val customRuleSets =
                        listOf(
                            libs.ktlint.ruleset.standard,
                            libs.nlopez.compose.rules,
                            libs.twitter.compose.rules,
                        ).map {
                            it.get().toString()
                        }

                    ktlint()
                        .customRuleSets(customRuleSets)
                        .editorConfigOverride(
                            mapOf(
                                "ktlint_standard_no-consecutive-comments" to RULE_DISABLED,
                                "ktlint_compose_compositionlocal-allowlist" to RULE_DISABLED,
                                "ktlint_standard_max-line-length" to RULE_DISABLED,
                            ),
                        )

                    endWithNewline()
                }

                // Don't add spotless as dependency for the Gradle's check task to facilitate separated codebase checks
                isEnforceCheck = false
            }
        }
    }
}
