package com.example.cinemate.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cinemate.R
import com.example.cinemate.model.Movie

class MovieAdapter(private val movieList: List<Movie>) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movieList[position]
        holder.title.text = movie.title
        holder.description.text = movie.description
        //holder.rating.text = movie.rating.toString()

        // Load poster image using Glide
        Glide.with(holder.itemView.context)
            .load(movie.posterUrl)
            .into(holder.poster)
    }

    override fun getItemCount(): Int {
        return movieList.size
    }

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_movie_title)
        val description: TextView = itemView.findViewById(R.id.tv_movie_description)
        //val rating: TextView = itemView.findViewById(R.id.tv_movie_rating)
        val poster: ImageView = itemView.findViewById(R.id.iv_movie_poster)
    }
}
