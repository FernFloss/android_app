package com.auditory.trackoccupancy.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CityTest {

    @Test
    fun `City data class creates correctly with valid data`() {
        // Given
        val localizedName = LocalizedString(ru = "Москва", en = "Moscow")
        val cityId = 1L

        // When
        val city = City(id = cityId, name = localizedName)

        // Then
        assertEquals(cityId, city.id)
        assertEquals(localizedName, city.name)
    }

    @Test
    fun `City equality works correctly`() {
        // Given
        val localizedName1 = LocalizedString(ru = "Москва", en = "Moscow")
        val localizedName2 = LocalizedString(ru = "Москва", en = "Moscow")
        val city1 = City(id = 1L, name = localizedName1)
        val city2 = City(id = 1L, name = localizedName2)

        // Then
        assertEquals(city1, city2)
    }

    @Test
    fun `City copy works correctly`() {
        // Given
        val originalCity = City(
            id = 1L,
            name = LocalizedString(ru = "Москва", en = "Moscow")
        )

        // When
        val copiedCity = originalCity.copy(
            name = LocalizedString(ru = "Санкт-Петербург", en = "Saint Petersburg")
        )

        // Then
        assertEquals(1L, copiedCity.id)
        assertEquals("Санкт-Петербург", copiedCity.name.ru)
        assertEquals("Saint Petersburg", copiedCity.name.en)
    }

    @Test
    fun `City toString works correctly`() {
        // Given
        val city = City(
            id = 1L,
            name = LocalizedString(ru = "Москва", en = "Moscow")
        )

        // When
        val toString = city.toString()

        // Then
        assert(toString.contains("City"))
        assert(toString.contains("id=1"))
        assert(toString.contains("name="))
    }
}
