package com.example.cinemate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cinemate.databinding.ItemTrendingMovieBinding
import com.example.cinemate.data.model.Movie

class TrendingMoviesAdapter(
    private var trendingMovies: List<Movie>,
    private val onItemClick: (Movie) -> Unit // Menambahkan parameter untuk menangani klik
) : RecyclerView.Adapter<TrendingMoviesAdapter.TrendingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendingViewHolder {
        val binding = ItemTrendingMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrendingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrendingViewHolder, position: Int) {
        if (trendingMovies.isNotEmpty()) {
            val movie = trendingMovies[position % trendingMovies.size]
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

    inner class TrendingViewHolder(private val binding: ItemTrendingMovieBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.tvMovieTitle.text = movie.title
            binding.tvMovieGenre.text = movie.genre
            binding.tvMovieRating.text = movie.rating.toString()
            binding.tvMovieDuration.text = "${movie.duration} min"
            Glide.with(binding.root.context)
                .load(movie.posterUrl)
                .into(binding.ivMoviePoster)

            // Menangani klik pada item
            binding.root.setOnClickListener {
                onItemClick(movie) // Memanggil fungsi klik dengan objek movie
            }
        }
    }

    fun updateMovies(newMovies: List<Movie>) {
        trendingMovies = newMovies
        notifyDataSetChanged()
    }
}
