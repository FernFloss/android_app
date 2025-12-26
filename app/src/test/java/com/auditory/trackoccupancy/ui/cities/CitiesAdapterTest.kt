package com.auditory.trackoccupancy.ui.cities

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.auditory.trackoccupancy.data.model.City
import com.auditory.trackoccupancy.data.model.LocalizedString
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.util.*

class CitiesAdapterTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockConfiguration: Configuration

    @Mock
    private lateinit var mockParent: ViewGroup

    @Mock
    private lateinit var mockLayoutInflater: LayoutInflater

    private lateinit var adapter: CitiesAdapter
    private lateinit var onCityClickCallback: (City) -> Unit
    private var clickedCity: City? = null

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        onCityClickCallback = { city -> clickedCity = city }
        adapter = CitiesAdapter(onCityClickCallback)

        // Mock LayoutInflater
        `when`(mockParent.context).thenReturn(mockContext)
        `when`(mockContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).thenReturn(mockLayoutInflater)
    }

    @Test
    fun `getItemCount returns correct count after submitting list`() {
        // Given
        val cities = listOf(
            City(id = 1L, name = LocalizedString(ru = "Москва", en = "Moscow")),
            City(id = 2L, name = LocalizedString(ru = "СПб", en = "Saint Petersburg"))
        )

        // When
        adapter.submitList(cities)

        // Then
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `getItemCount returns 0 for null list`() {
        // When
        adapter.submitList(null)

        // Then
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `getItemCount returns 0 for empty list`() {
        // When
        adapter.submitList(emptyList())

        // Then
        assertEquals(0, adapter.itemCount)
    }

    // Note: DiffCallback testing would require making it public or using reflection.
    // For unit tests, we focus on testing the observable behavior of the adapter.

    // Note: Testing onCreateViewHolder and onBindViewHolder would require more complex mocking
    // of Android View components, which is typically done with Robolectric or Android Test Kit.
    // For unit tests, we focus on testing the business logic and data transformations.
}
