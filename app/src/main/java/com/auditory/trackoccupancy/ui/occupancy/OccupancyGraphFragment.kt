package com.auditory.trackoccupancy.ui.occupancy

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.auditory.trackoccupancy.R
import com.auditory.trackoccupancy.databinding.FragmentOccupancyGraphBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class OccupancyGraphFragment : Fragment() {

    private var _binding: FragmentOccupancyGraphBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OccupancyGraphViewModel by viewModels()
    private val args: OccupancyGraphFragmentArgs by navArgs()
    private lateinit var occupancyDataAdapter: OccupancyDataAdapter
    private var selectedDate: String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        .format(java.util.Date()) // Default to today

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOccupancyGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupDatePickerButton()
        observeViewModel()
        loadData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        occupancyDataAdapter = OccupancyDataAdapter()
        binding.occupancyRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = occupancyDataAdapter
        }
    }


    private fun setupDatePickerButton() {
        binding.changeDateButton.setOnClickListener {
            showDatePickerDialog()
        }
        updateSelectedDateDisplay()
    }

    private fun showDatePickerDialog() {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH)
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                updateSelectedDateDisplay()
                loadData()
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun updateSelectedDateDisplay() {
        val formattedDate = try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val date = inputFormat.parse(selectedDate)
            outputFormat.format(date ?: java.util.Date())
        } catch (e: Exception) {
            selectedDate
        }

        binding.selectedDateTextView.text = getString(R.string.selected_date, formattedDate)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is OccupancyGraphUiState.Loading -> {
                        binding.loadingProgressBar.visibility = View.VISIBLE
                        binding.errorTextView.visibility = View.GONE
                        binding.graphContainer.visibility = View.GONE
                    }
                    is OccupancyGraphUiState.Success -> {
                        binding.loadingProgressBar.visibility = View.GONE
                        binding.errorTextView.visibility = View.GONE
                        binding.graphContainer.visibility = View.VISIBLE

                        updateOccupancyData(state.data)
                    }
                    is OccupancyGraphUiState.Empty -> {
                        binding.loadingProgressBar.visibility = View.GONE
                        binding.errorTextView.visibility = View.VISIBLE
                        binding.graphContainer.visibility = View.GONE
                        binding.errorTextView.text = getString(R.string.statistics_unavailable)
                        Log.d("OccupancyGraphFragment", "No statistics available for the selected date")
                    }
                    is OccupancyGraphUiState.Error -> {
                        binding.loadingProgressBar.visibility = View.GONE
                        binding.errorTextView.visibility = View.VISIBLE
                        binding.graphContainer.visibility = View.GONE
                        binding.errorTextView.text = state.message
                        Log.e("OccupancyGraphFragment", "Error loading occupancy data: ${state.message}")
                    }
                }
            }
        }
    }

    private fun loadData() {
        viewModel.loadOccupancyHistory(args.cityId, args.buildingId, args.auditoriumId, selectedDate)
    }

    private fun updateOccupancyData(data: List<OccupancyDataPoint>) {
        if (data.isEmpty()) {
            occupancyDataAdapter.submitList(emptyList())
            return
        }

        // Sort data by timestamp (newest first)
        val sortedData = data.sortedByDescending { it.timestamp }
        occupancyDataAdapter.submitList(sortedData)

        // Update title - just the auditorium name (already includes "Auditorium")
        binding.auditoriumTitleTextView.text = args.auditoriumName
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

