package com.example.cinemate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cinemate.databinding.ItemTrendingMovieBinding
import com.example.cinemate.model.Movie

class TrendingMoviesAdapter(
    private var trendingMovies: List<Movie>
) : RecyclerView.Adapter<TrendingMoviesAdapter.TrendingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendingViewHolder {
        val binding = ItemTrendingMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrendingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrendingViewHolder, position: Int) {
        if (trendingMovies.isNotEmpty()) {
            val movie = trendingMovies[position % trendingMovies.size] // Infinite scrolling
            holder.bind(movie)
        }
    }

    override fun getItemCount(): Int {
        return if (trendingMovies.isNotEmpty()) {
            Int.MAX_VALUE // Mengembalikan nilai maksimum untuk infinite scrolling
        } else {
            0 // Jika tidak ada film, kembalikan 0
        }
    }

    class TrendingViewHolder(private val binding: ItemTrendingMovieBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.tvMovieTitle.text = movie.title // Pastikan judul diatur di sini
            Glide.with(binding.root.context).load(movie.posterUrl).into(binding.ivMoviePoster)
        }
    }

    fun updateMovies(newMovies: List<Movie>) {
        trendingMovies = newMovies
        notifyDataSetChanged()
    }
}
