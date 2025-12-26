package com.auditory.trackoccupancy.data.model

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.util.*

class LocalizedStringTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockConfiguration: Configuration

    private lateinit var localizedString: LocalizedString

    fun setUp() {
        MockitoAnnotations.openMocks(this)
        localizedString = LocalizedString(
            ru = "Русский текст",
            en = "English text"
        )
    }

    @Test
    fun `getLocalizedValue returns English text for English locale`() {
        setUp()
        // Given
        val englishLocale = Locale.ENGLISH
        val localeList = LocaleList(englishLocale)
        `when`(mockConfiguration.locales).thenReturn(localeList)
        `when`(mockContext.resources.configuration).thenReturn(mockConfiguration)

        // When
        val result = localizedString.getLocalizedValue(mockContext)

        // Then
        assertEquals("English text", result)
    }

    @Test
    fun `getLocalizedValue returns Russian text for Russian locale`() {
        setUp()
        // Given
        val russianLocale = Locale("ru")
        val localeList = LocaleList(russianLocale)
        `when`(mockConfiguration.locales).thenReturn(localeList)
        `when`(mockContext.resources.configuration).thenReturn(mockConfiguration)

        // When
        val result = localizedString.getLocalizedValue(mockContext)

        // Then
        assertEquals("Русский текст", result)
    }

    @Test
    fun `getLocalizedValue returns English text for unsupported locale`() {
        setUp()
        // Given
        val germanLocale = Locale.GERMAN
        val localeList = LocaleList(germanLocale)
        `when`(mockConfiguration.locales).thenReturn(localeList)
        `when`(mockContext.resources.configuration).thenReturn(mockConfiguration)

        // When
        val result = localizedString.getLocalizedValue(mockContext)

        // Then
        assertEquals("English text", result)
    }

    @Test
    fun `getLocalizedValue returns English text for empty locale list`() {
        setUp()
        // Given
        val emptyLocaleList = LocaleList.getEmptyLocaleList()
        `when`(mockConfiguration.locales).thenReturn(emptyLocaleList)
        `when`(mockContext.resources.configuration).thenReturn(mockConfiguration)

        // When
        val result = localizedString.getLocalizedValue(mockContext)

        // Then
        assertEquals("English text", result)
    }
}
