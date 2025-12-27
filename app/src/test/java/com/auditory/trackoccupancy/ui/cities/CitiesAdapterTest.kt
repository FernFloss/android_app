package com.auditory.trackoccupancy.ui.cities

import com.auditory.trackoccupancy.data.model.City
import com.auditory.trackoccupancy.data.model.LocalizedString
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for CitiesAdapter data classes and DiffUtil logic.
 * 
 * Note: Testing the adapter's submitList(), onCreateViewHolder(), and onBindViewHolder() 
 * requires Android RecyclerView runtime which is not available in pure unit tests. 
 * Those tests should be in androidTest with Robolectric or on-device instrumented tests.
 * 
 * This file tests the data classes used by the adapter.
 */
class CitiesAdapterTest {

    @Test
    fun `City equality works correctly for DiffUtil`() {
        // Given - same IDs
        val city1 = City(id = 1L, name = LocalizedString(ru = "Москва", en = "Moscow"))
        val city2 = City(id = 1L, name = LocalizedString(ru = "Москва", en = "Moscow"))

        // Then
        assertEquals(city1, city2)
        assertEquals(city1.id, city2.id)
    }

    @Test
    fun `Cities with same ID but different names are not equal`() {
        // Given
        val city1 = City(id = 1L, name = LocalizedString(ru = "Москва", en = "Moscow"))
        val city2 = City(id = 1L, name = LocalizedString(ru = "Санкт-Петербург", en = "Saint Petersburg"))

        // Then - same ID but different content
        assertEquals(city1.id, city2.id)
        assertNotEquals(city1, city2)
    }

    @Test
    fun `Cities with different IDs are not equal`() {
        // Given
        val city1 = City(id = 1L, name = LocalizedString(ru = "Москва", en = "Moscow"))
        val city2 = City(id = 2L, name = LocalizedString(ru = "Москва", en = "Moscow"))

        // Then
        assertNotEquals(city1.id, city2.id)
        assertNotEquals(city1, city2)
    }

    @Test
    fun `City copy creates correct instance`() {
        // Given
        val original = City(id = 1L, name = LocalizedString(ru = "Москва", en = "Moscow"))

        // When
        val copied = original.copy(id = 2L)

        // Then
        assertEquals(2L, copied.id)
        assertEquals(original.name, copied.name)
    }

    @Test
    fun `City list operations work correctly`() {
        // Given
        val cities = listOf(
            City(id = 1L, name = LocalizedString(ru = "Москва", en = "Moscow")),
            City(id = 2L, name = LocalizedString(ru = "СПб", en = "Saint Petersburg")),
            City(id = 3L, name = LocalizedString(ru = "Казань", en = "Kazan"))
        )

        // Then
        assertEquals(3, cities.size)
        assertEquals(1L, cities[0].id)
        assertEquals(2L, cities[1].id)
        assertEquals(3L, cities[2].id)
    }

    @Test
    fun `Finding city by ID works correctly`() {
        // Given
        val cities = listOf(
            City(id = 1L, name = LocalizedString(ru = "Москва", en = "Moscow")),
            City(id = 2L, name = LocalizedString(ru = "СПб", en = "Saint Petersburg"))
        )

        // When
        val found = cities.find { it.id == 2L }

        // Then
        assertNotNull(found)
        assertEquals("Saint Petersburg", found?.name?.en)
    }
}
