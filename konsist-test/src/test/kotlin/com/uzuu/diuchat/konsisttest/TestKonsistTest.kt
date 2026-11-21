package com.uzuu.diuchat.konsisttest

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.functions
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.jupiter.api.Test

// Check test coding rules.
class TestKonsistTest {
    @Test
    fun `local tests use JUnit Jupiter while instrumented tests may use AndroidJUnit4`() {
        Konsist
            .scopeFromProject()
            .slice { !it.resideInPath("..androidTest..") }
            .classes()
            .functions()
            .assertFalse { it.hasAnnotationsWithAllNames("org.junit.Test") } // should be only org.junit.jupiter.api.Test
    }
}
