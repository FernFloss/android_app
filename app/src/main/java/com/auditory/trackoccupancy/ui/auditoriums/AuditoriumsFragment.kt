package com.auditory.trackoccupancy.ui.auditoriums

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
import com.auditory.trackoccupancy.databinding.FragmentAuditoriumsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuditoriumsFragment : Fragment() {

    private var _binding: FragmentAuditoriumsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuditoriumsViewModel by viewModels()
    private val args: AuditoriumsFragmentArgs by navArgs()
    private lateinit var auditoriumsAdapter: AuditoriumsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuditoriumsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("AuditoriumsFragment", "Loading auditoriums for building: ${args.buildingId} (${args.buildingName}) in city: ${args.cityId}")

        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()

        viewModel.loadAuditoriums(args.cityId, args.buildingId)
    }

    private fun setupRecyclerView() {
        auditoriumsAdapter = AuditoriumsAdapter { auditoriumWithOccupancy ->
            val auditorium = auditoriumWithOccupancy.auditorium
            Log.d("AuditoriumsFragment", "Camera selected for auditorium: ${auditorium.auditoriumNumber}")
            // Navigate to camera view for live feed
            val action = AuditoriumsFragmentDirections.actionAuditoriumsToCamera(
                cityId = args.cityId,
                buildingId = args.buildingId,
                auditoriumId = auditorium.id,
                auditoriumName = "${getString(R.string.auditorium_label)} ${auditorium.auditoriumNumber}"
            )
            findNavController().navigate(action)
        }

        binding.auditoriumsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = auditoriumsAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadAuditoriums(args.cityId, args.buildingId)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AuditoriumsUiState.Loading -> {
                        binding.loadingProgressBar.visibility = View.VISIBLE
                        binding.auditoriumsRecyclerView.visibility = View.GONE
                        binding.errorTextView.visibility = View.GONE
                        binding.swipeRefreshLayout.isRefreshing = true
                    }
                    is AuditoriumsUiState.Success -> {
                        binding.loadingProgressBar.visibility = View.GONE
                        binding.auditoriumsRecyclerView.visibility = View.VISIBLE
                        binding.errorTextView.visibility = View.GONE
                        binding.swipeRefreshLayout.isRefreshing = false
                        auditoriumsAdapter.submitList(state.auditoriumsWithOccupancy)
                        Log.d("AuditoriumsFragment", "Loaded ${state.auditoriumsWithOccupancy.size} auditoriums with occupancy data")
                    }
                    is AuditoriumsUiState.Error -> {
                        binding.loadingProgressBar.visibility = View.GONE
                        binding.auditoriumsRecyclerView.visibility = View.GONE
                        binding.errorTextView.visibility = View.VISIBLE
                        binding.swipeRefreshLayout.isRefreshing = false
                        binding.errorTextView.text = getString(R.string.auditorium_list_unavailable_error)
                        auditoriumsAdapter.submitList(emptyList())
                        Log.e("AuditoriumsFragment", "Error loading auditoriums: ${state.message}")
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
