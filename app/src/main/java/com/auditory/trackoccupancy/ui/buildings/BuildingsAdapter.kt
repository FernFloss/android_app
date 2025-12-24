package com.auditory.trackoccupancy.ui.buildings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.auditory.trackoccupancy.data.model.Building
import com.auditory.trackoccupancy.databinding.ItemBuildingBinding

class BuildingsAdapter(
    private val onBuildingClick: (Building) -> Unit
) : ListAdapter<Building, BuildingsAdapter.BuildingViewHolder>(BuildingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuildingViewHolder {
        val binding = ItemBuildingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BuildingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BuildingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BuildingViewHolder(
        private val binding: ItemBuildingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onBuildingClick(getItem(position))
                }
            }
        }

        fun bind(building: Building) {
            binding.buildingNameTextView.text = building.address.getLocalizedValue(binding.root.context)
            binding.buildingAddressTextView.visibility = android.view.View.GONE
        }
    }
}

class BuildingDiffCallback : DiffUtil.ItemCallback<Building>() {
    override fun areItemsTheSame(oldItem: Building, newItem: Building): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Building, newItem: Building): Boolean {
        return oldItem == newItem
    }
}
