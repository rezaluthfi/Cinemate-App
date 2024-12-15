package com.example.cinemate.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cinemate.R
import com.example.cinemate.databinding.ItemMovieBinding
import com.example.cinemate.data.model.Movie
import com.google.android.flexbox.FlexboxLayout

class AllMoviesAdapter(
    private var movies: List<Movie>,
    private val onItemClick: (Movie) -> Unit // Menambahkan parameter untuk menangani klik
) : RecyclerView.Adapter<AllMoviesAdapter.MovieViewHolder>() {

    class MovieViewHolder(private val binding: ItemMovieBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie, onItemClick: (Movie) -> Unit) {
            binding.tvMovieTitle.text = movie.title
            binding.tvMovieDescription.text = movie.description
            binding.tvMovieDuration.text = "${movie.duration} min"
            binding.tvMovieGenre.text = movie.genre

            // Menghapus semua TextView bioskop yang ada sebelumnya
            binding.flCinemas.removeAllViews()

            // Menambahkan TextView untuk setiap bioskop
            movie.cinemas.forEach { cinema ->
                val cinemaTextView = TextView(binding.root.context).apply {
                    id = View.generateViewId() // Menghasilkan ID unik untuk setiap TextView
                    text = cinema
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    setTextColor(ContextCompat.getColor(binding.root.context, R.color.white_60))
                    setBackgroundResource(R.drawable.bg_genre) // Menggunakan background yang sama
                    setPadding(16, 8, 16, 8) // Padding kiri, atas, kanan, bawah
                    layoutParams = FlexboxLayout.LayoutParams(
                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                        FlexboxLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 16, 16) // Margin kiri, atas, kanan, bawah
                    }
                }
                binding.flCinemas.addView(cinemaTextView) // Menambahkan TextView ke FlexboxLayout
            }

            Glide.with(binding.ivMoviePoster.context)
                .load(movie.posterUrl)
                .into(binding.ivMoviePoster)

            // Menangani klik pada item
            binding.root.setOnClickListener {
                onItemClick(movie) // Memanggil fungsi klik dengan objek movie
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position], onItemClick) // Mengoper fungsi klik ke bind
    }

    override fun getItemCount(): Int = movies.size

    fun updateMovies(newMovies: List<Movie>) {
        movies = newMovies
        notifyDataSetChanged()
    }
}
