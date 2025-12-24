package com.auditory.trackoccupancy.ui.occupancy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.auditory.trackoccupancy.R
import com.auditory.trackoccupancy.databinding.ItemOccupancyDataBinding
import java.text.SimpleDateFormat
import java.util.*

class OccupancyDataAdapter : ListAdapter<OccupancyDataPoint, OccupancyDataAdapter.OccupancyDataViewHolder>(OccupancyDataDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OccupancyDataViewHolder {
        val binding = ItemOccupancyDataBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OccupancyDataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OccupancyDataViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OccupancyDataViewHolder(
        private val binding: ItemOccupancyDataBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(dataPoint: OccupancyDataPoint) {
            val context = binding.root.context

            // Format time
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            binding.timeTextView.text = timeFormat.format(Date(dataPoint.timestamp))

            // Format: avg_person_count / capacity (percentage%)
            val percentage = if (dataPoint.capacity > 0) {
                (dataPoint.avgPersonCount / dataPoint.capacity * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }
            binding.percentageTextView.text = "${dataPoint.avgPersonCount.toInt()} / ${dataPoint.capacity} (${percentage}%)"

            // Set progress bar (using calculated percentage)
            binding.occupancyProgressBar.max = 100
            binding.occupancyProgressBar.progress = percentage

            // Set progress bar color based on occupancy percentage
            val color = when {
                percentage >= 90 -> ContextCompat.getColor(context, R.color.occupancy_high)
                percentage >= 70 -> ContextCompat.getColor(context, R.color.occupancy_medium)
                else -> ContextCompat.getColor(context, R.color.occupancy_low)
            }
            binding.occupancyProgressBar.progressTintList = android.content.res.ColorStateList.valueOf(color)

            // Format date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.dateTextView.text = dateFormat.format(Date(dataPoint.timestamp))
        }
    }
}

class OccupancyDataDiffCallback : DiffUtil.ItemCallback<OccupancyDataPoint>() {
    override fun areItemsTheSame(oldItem: OccupancyDataPoint, newItem: OccupancyDataPoint): Boolean {
        return oldItem.timestamp == newItem.timestamp
    }

    override fun areContentsTheSame(oldItem: OccupancyDataPoint, newItem: OccupancyDataPoint): Boolean {
        return oldItem == newItem
    }
}
