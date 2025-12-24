package com.auditory.trackoccupancy.ui.cities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.auditory.trackoccupancy.data.model.City
import com.auditory.trackoccupancy.databinding.ItemCityBinding

class CitiesAdapter(
    private val onCityClick: (City) -> Unit
) : ListAdapter<City, CitiesAdapter.CityViewHolder>(CityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val binding = ItemCityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CityViewHolder(binding, onCityClick)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CityViewHolder(
        private val binding: ItemCityBinding,
        private val onCityClick: (City) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(city: City) {
            binding.cityNameTextView.text = city.name.getLocalizedValue(binding.root.context)
            binding.root.setOnClickListener { onCityClick(city) }
        }
    }

    private class CityDiffCallback : DiffUtil.ItemCallback<City>() {
        override fun areItemsTheSame(oldItem: City, newItem: City): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: City, newItem: City): Boolean {
            return oldItem == newItem
        }
    }
}
