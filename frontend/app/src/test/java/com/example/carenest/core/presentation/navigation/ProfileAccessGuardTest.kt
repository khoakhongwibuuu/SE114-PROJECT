package com.example.carenest.core.presentation.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileAccessGuardTest {

    @Test
    fun isValidHealthProfileId_returnsFalseForNullAndNonPositiveIds() {
        assertFalse((null as Long?).isValidHealthProfileId())
        assertFalse(0L.isValidHealthProfileId())
        assertFalse((-1L).isValidHealthProfileId())
    }

    @Test
    fun isValidHealthProfileId_returnsTrueForPositiveId() {
        assertTrue(42L.isValidHealthProfileId())
    }
}
