package com.auditory.trackoccupancy.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Unit tests for LocalizedString data class.
 * 
 * Note: Testing getLocalizedValue() requires Android Context which is not available
 * in pure unit tests. Those tests should be in androidTest with Robolectric or
 * on-device instrumented tests.
 */
class LocalizedStringTest {

    @Test
    fun `LocalizedString correctly stores Russian text`() {
        // Given
        val localizedString = LocalizedString(ru = "Русский текст", en = "English text")

        // Then
        assertEquals("Русский текст", localizedString.ru)
    }

    @Test
    fun `LocalizedString correctly stores English text`() {
        // Given
        val localizedString = LocalizedString(ru = "Русский текст", en = "English text")

        // Then
        assertEquals("English text", localizedString.en)
    }

    @Test
    fun `LocalizedString equality works correctly`() {
        // Given
        val string1 = LocalizedString(ru = "Привет", en = "Hello")
        val string2 = LocalizedString(ru = "Привет", en = "Hello")
        val string3 = LocalizedString(ru = "Пока", en = "Goodbye")

        // Then
        assertEquals(string1, string2)
        assertNotEquals(string1, string3)
    }

    @Test
    fun `LocalizedString copy works correctly`() {
        // Given
        val original = LocalizedString(ru = "Русский", en = "English")

        // When
        val copied = original.copy(en = "Modified English")

        // Then
        assertEquals("Русский", copied.ru)
        assertEquals("Modified English", copied.en)
    }

    @Test
    fun `LocalizedString hashCode is consistent`() {
        // Given
        val string1 = LocalizedString(ru = "Привет", en = "Hello")
        val string2 = LocalizedString(ru = "Привет", en = "Hello")

        // Then
        assertEquals(string1.hashCode(), string2.hashCode())
    }

    @Test
    fun `LocalizedString handles empty strings`() {
        // Given
        val emptyRu = LocalizedString(ru = "", en = "English")
        val emptyEn = LocalizedString(ru = "Русский", en = "")
        val bothEmpty = LocalizedString(ru = "", en = "")

        // Then
        assertEquals("", emptyRu.ru)
        assertEquals("English", emptyRu.en)
        assertEquals("Русский", emptyEn.ru)
        assertEquals("", emptyEn.en)
        assertEquals("", bothEmpty.ru)
        assertEquals("", bothEmpty.en)
    }
}
