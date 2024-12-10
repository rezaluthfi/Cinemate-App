package com.example.cinemate.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cinemate.R
import com.example.cinemate.databinding.ItemMovieBinding
import com.example.cinemate.model.Movie
import com.google.android.flexbox.FlexboxLayout

class LatestMoviesAdapter(private var latestMovies: List<Movie>) : RecyclerView.Adapter<LatestMoviesAdapter.LatestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LatestViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LatestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LatestViewHolder, position: Int) {
        val movie = latestMovies[position]
        holder.bind(movie)
    }

    override fun getItemCount(): Int = latestMovies.size

    class LatestViewHolder(private val binding: ItemMovieBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.tvMovieTitle.text = movie.title
            binding.tvMovieDescription.text = movie.description
            binding.tvDuration.text = "${movie.duration} min"
            binding.tvCategories.text = movie.categories[0] // Mengambil kategori pertama

            // Menghapus semua TextView bioskop yang ada sebelumnya
            binding.llCinemas.removeAllViews()

            // Menambahkan TextView untuk setiap bioskop
            movie.cinemas.forEach { cinema ->
                val cinemaTextView = TextView(binding.root.context).apply {
                    id = View.generateViewId() // Menghasilkan ID unik untuk setiap TextView
                    text = cinema
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    setTextColor(ContextCompat.getColor(binding.root.context, R.color.white_60))
                    setBackgroundResource(R.drawable.bg_category) // Menggunakan background yang sama
                    setPadding(16, 8, 16, 8) // Padding kiri, atas, kanan, bawah
                    layoutParams = FlexboxLayout.LayoutParams(
                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                        FlexboxLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 16, 16) // Margin kiri, atas, kanan, bawah
                    }
                }
                binding.llCinemas.addView(cinemaTextView) // Menambahkan TextView ke FlexboxLayout
            }

            // Memuat gambar poster film
            Glide.with(binding.root.context)
                .load(movie.posterUrl)
                .into(binding.ivMoviePoster)
        }
    }

    fun updateMovies(newMovies: List<Movie>) {
        latestMovies = newMovies
        notifyDataSetChanged()
    }
}

