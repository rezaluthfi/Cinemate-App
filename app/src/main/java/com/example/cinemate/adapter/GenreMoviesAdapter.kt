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
import com.example.cinemate.model.Movie
import com.google.android.flexbox.FlexboxLayout

class GenreMoviesAdapter(
    private var genreMovies: List<Movie>,
    private val onItemClick: (Movie) -> Unit // Menambahkan parameter untuk menangani klik
) : RecyclerView.Adapter<GenreMoviesAdapter.GenreViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GenreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        val movie = genreMovies[position]
        holder.bind(movie)
    }

    override fun getItemCount(): Int = genreMovies.size

    inner class GenreViewHolder(private val binding: ItemMovieBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
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

            // Memuat gambar poster film
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
        genreMovies = newMovies
        notifyDataSetChanged()
    }
}
