package com.example.cinemate.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cinemate.databinding.ItemSlideOnboardingBinding

class SliderAdapter(
    private val onboardingItems: List<OnboardingItem>
) : RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val binding = ItemSlideOnboardingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SliderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        val item = onboardingItems[position]
        holder.binding.imageView.setImageResource(item.image)
    }

    override fun getItemCount(): Int = onboardingItems.size

    class SliderViewHolder(val binding: ItemSlideOnboardingBinding) : RecyclerView.ViewHolder(binding.root)
}