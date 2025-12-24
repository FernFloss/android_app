package com.auditory.trackoccupancy.ui.auditoriums

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.auditory.trackoccupancy.R
import com.auditory.trackoccupancy.databinding.ItemAuditoriumBinding

class AuditoriumsAdapter(
    private val onCameraClick: (AuditoriumWithOccupancy) -> Unit
) : ListAdapter<AuditoriumWithOccupancy, AuditoriumsAdapter.AuditoriumViewHolder>(AuditoriumDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuditoriumViewHolder {
        val binding = ItemAuditoriumBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AuditoriumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AuditoriumViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AuditoriumViewHolder(
        private val binding: ItemAuditoriumBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.cameraButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCameraClick(getItem(position))
                }
            }
        }

        fun bind(auditoriumWithOccupancy: AuditoriumWithOccupancy) {
            val auditorium = auditoriumWithOccupancy.auditorium
            val context = binding.root.context

            binding.auditoriumNameTextView.text = "${context.getString(R.string.auditorium_label)} ${auditorium.auditoriumNumber}"
            binding.auditoriumCapacityTextView.text = "${context.getString(R.string.capacity_label)} ${auditorium.capacity}"

            // Show current occupancy and percentage
            val occupancyText = "${auditoriumWithOccupancy.currentOccupancy}/${auditorium.capacity} (${auditoriumWithOccupancy.occupancyPercentage}%)"
            binding.cameraMacTextView.text = "${context.getString(R.string.occupied_label)} $occupancyText"

            // Color code based on occupancy percentage
            val textColor = when {
                auditoriumWithOccupancy.occupancyPercentage >= 90 -> ContextCompat.getColor(context, R.color.occupancy_high)
                auditoriumWithOccupancy.occupancyPercentage >= 70 -> ContextCompat.getColor(context, R.color.occupancy_medium)
                else -> ContextCompat.getColor(context, R.color.occupancy_low)
            }
            binding.cameraMacTextView.setTextColor(textColor)

            // Enable camera button
            binding.cameraButton.isEnabled = true // Assume cameras are available for now
        }
    }
}

class AuditoriumDiffCallback : DiffUtil.ItemCallback<AuditoriumWithOccupancy>() {
    override fun areItemsTheSame(oldItem: AuditoriumWithOccupancy, newItem: AuditoriumWithOccupancy): Boolean {
        return oldItem.auditorium.id == newItem.auditorium.id
    }

    override fun areContentsTheSame(oldItem: AuditoriumWithOccupancy, newItem: AuditoriumWithOccupancy): Boolean {
        return oldItem == newItem
    }
}
