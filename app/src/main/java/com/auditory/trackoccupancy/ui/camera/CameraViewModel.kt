package com.auditory.trackoccupancy.ui.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import com.auditory.trackoccupancy.data.model.Camera
import com.auditory.trackoccupancy.data.repository.OccupancyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.ByteArrayInputStream
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val repository: OccupancyRepository
) : ViewModel() {

    suspend fun getCameras(cityId: Long, buildingId: Long, auditoriumId: Long): Result<List<Camera>> {
        return repository.getCamerasByAuditorium(cityId, buildingId, auditoriumId)
    }

    suspend fun getCameraSnapshot(mac: String): Result<Bitmap> {
        return try {
            val result = repository.getCameraSnapshot(mac)
            result.fold(
                onSuccess = { byteArray ->
                    val bitmap = ByteArrayInputStream(byteArray).use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                    if (bitmap != null) {
                        Result.success(bitmap)
                    } else {
                        Result.failure(Exception("Failed to decode image"))
                    }
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
