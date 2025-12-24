package com.auditory.trackoccupancy.ui.cities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.auditory.trackoccupancy.databinding.FragmentCitiesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CitiesFragment : Fragment() {

    private var _binding: FragmentCitiesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CitiesViewModel by viewModels()
    private lateinit var citiesAdapter: CitiesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCitiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()

        viewModel.loadCities()
    }

    private fun setupRecyclerView() {
        citiesAdapter = CitiesAdapter { city ->
            val action = CitiesFragmentDirections.actionCitiesToBuildings(
                cityId = city.id,
                cityName = city.name.en // or city.name.ru based on locale
            )
            findNavController().navigate(action)
        }

        binding.citiesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = citiesAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadCities()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is CitiesUiState.Loading -> {
                        binding.loadingProgressBar.visibility = View.VISIBLE
                        binding.citiesRecyclerView.visibility = View.GONE
                        binding.errorTextView.visibility = View.GONE
                        binding.swipeRefreshLayout.isRefreshing = true
                    }
                    is CitiesUiState.Success -> {
                        binding.loadingProgressBar.visibility = View.GONE
                        binding.citiesRecyclerView.visibility = View.VISIBLE
                        binding.errorTextView.visibility = View.GONE
                        binding.swipeRefreshLayout.isRefreshing = false
                        citiesAdapter.submitList(state.cities)
                    }
                    is CitiesUiState.Error -> {
                        binding.loadingProgressBar.visibility = View.GONE
                        binding.citiesRecyclerView.visibility = View.GONE
                        binding.errorTextView.visibility = View.VISIBLE
                        binding.swipeRefreshLayout.isRefreshing = false
                        binding.errorTextView.text = state.message
                        citiesAdapter.submitList(emptyList())
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
