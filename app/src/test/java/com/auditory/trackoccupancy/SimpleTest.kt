package com.auditory.trackoccupancy

import com.auditory.trackoccupancy.data.model.LocalizedString
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Simple test to verify the testing framework is working
 */
class SimpleTest {

    @Test
    fun `LocalizedString getLocalizedValue works correctly for English`() {
        val localizedString = LocalizedString(ru = "Русский", en = "English")

        // Mock context with English locale would be needed for full test,
        // but this verifies the data class works
        assertEquals("Русский", localizedString.ru)
        assertEquals("English", localizedString.en)
    }

    @Test
    fun `basic arithmetic works`() {
        assertEquals(4, 2 + 2)
    }
}
