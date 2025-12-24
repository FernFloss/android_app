package com.auditory.trackoccupancy.ui.buildings

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
import com.auditory.trackoccupancy.databinding.FragmentBuildingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BuildingsFragment : Fragment() {

    private var _binding: FragmentBuildingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BuildingsViewModel by viewModels()
    private val args: BuildingsFragmentArgs by navArgs()
    private lateinit var buildingsAdapter: BuildingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuildingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("BuildingsFragment", "Loading buildings for city: ${args.cityId} (${args.cityName})")

        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()

        viewModel.loadBuildings(args.cityId)
    }

    private fun setupRecyclerView() {
        buildingsAdapter = BuildingsAdapter { building ->
            Log.d("BuildingsFragment", "Building selected: ${building.id}")
            val action = BuildingsFragmentDirections.actionBuildingsToAuditoriums(
                cityId = args.cityId,
                buildingId = building.id,
                buildingName = "Building ${building.id}" // or building.name.ru based on locale
            )
            findNavController().navigate(action)
        }

        binding.buildingsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = buildingsAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadBuildings(args.cityId)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is BuildingsUiState.Loading -> {
                        binding.loadingProgressBar.visibility = View.VISIBLE
                        binding.buildingsRecyclerView.visibility = View.GONE
                        binding.errorTextView.visibility = View.GONE
                        binding.swipeRefreshLayout.isRefreshing = true
                    }
                    is BuildingsUiState.Success -> {
                        binding.loadingProgressBar.visibility = View.GONE
                        binding.buildingsRecyclerView.visibility = View.VISIBLE
                        binding.errorTextView.visibility = View.GONE
                        binding.swipeRefreshLayout.isRefreshing = false
                        buildingsAdapter.submitList(state.buildings)
                        Log.d("BuildingsFragment", "Loaded ${state.buildings.size} buildings")
                    }
                    is BuildingsUiState.Error -> {
                        binding.loadingProgressBar.visibility = View.GONE
                        binding.buildingsRecyclerView.visibility = View.GONE
                        binding.errorTextView.visibility = View.VISIBLE
                        binding.swipeRefreshLayout.isRefreshing = false
                        binding.errorTextView.text = state.message
                        buildingsAdapter.submitList(emptyList())
                        Log.e("BuildingsFragment", "Error loading buildings: ${state.message}")
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
