package com.auditory.trackoccupancy.ui.camera

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.auditory.trackoccupancy.R
import com.auditory.trackoccupancy.databinding.FragmentCameraBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CameraViewModel by viewModels()
    private val args: CameraFragmentArgs by navArgs()

    private var cameraMac: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("CameraFragment", "Loading cameras for auditorium: ${args.auditoriumId} (${args.auditoriumName})")

        binding.auditoriumNameTextView.text = args.auditoriumName
        binding.cameraMacTextView.text = getString(R.string.loading)

        loadCameraData()
    }

    private fun loadCameraData() {
        Log.d("CameraFragment", "Starting to load camera data for auditorium ${args.auditoriumId}")
        binding.loadingProgressBar.visibility = View.VISIBLE
        binding.cameraImageView.visibility = View.GONE

        lifecycleScope.launch {
            Log.d("CameraFragment", "Making API call to get cameras")
            val result = viewModel.getCameras(args.cityId, args.buildingId, args.auditoriumId)
            result.fold(
                onSuccess = { cameras ->
                    Log.d("CameraFragment", "Camera API call successful, received ${cameras.size} cameras")
                    binding.loadingProgressBar.visibility = View.GONE
                    if (cameras.isNotEmpty()) {
                        cameraMac = cameras[0].mac // Use first camera's MAC
                        Log.d("CameraFragment", "Found camera with MAC: $cameraMac")
                        binding.cameraMacTextView.text = "${getString(R.string.camera_label)}: $cameraMac"
                        binding.cameraImageView.visibility = View.VISIBLE
                        startLiveFeed()
                    } else {
                        Log.w("CameraFragment", "No cameras found for auditorium ${args.auditoriumId}")
                        binding.cameraMacTextView.text = getString(R.string.no_data)
                        Toast.makeText(requireContext(), "No camera found for this auditorium", Toast.LENGTH_LONG).show()
                    }
                },
                onFailure = { exception ->
                    Log.e("CameraFragment", "Camera API call failed", exception)
                    binding.loadingProgressBar.visibility = View.GONE
                    Log.e("CameraFragment", "Failed to load camera data", exception)
                    binding.cameraMacTextView.text = getString(R.string.network_error)
                    Toast.makeText(requireContext(), "Failed to load camera: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun startLiveFeed() {
        val mac = cameraMac
        if (mac.isNullOrEmpty()) {
            Log.e("CameraFragment", "No camera MAC available")
            return
        }

        Log.d("CameraFragment", "Starting live feed updates every second for MAC: $mac")

        // Start live snapshot updates
        lifecycleScope.launch {
            while (isActive) {
                try {
                    val result = viewModel.getCameraSnapshot(mac)
                    result.fold(
                        onSuccess = { bitmap ->
                            binding.cameraImageView.setImageBitmap(bitmap)
                            Log.v("CameraFragment", "Snapshot updated successfully")
                        },
                        onFailure = { exception ->
                            Log.e("CameraFragment", "Failed to get snapshot", exception)
                            // Don't show error toast for every failed snapshot to avoid spam
                        }
                    )
                } catch (e: Exception) {
                    Log.e("CameraFragment", "Error in live feed loop", e)
                }

                delay(500) // Update every second
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Live feed will automatically stop when lifecycleScope is cancelled
        Log.d("CameraFragment", "Camera feed paused")
    }

    override fun onResume() {
        super.onResume()
        Log.d("CameraFragment", "Camera feed resumed")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("CameraFragment", "Camera fragment destroyed")
    }
}
