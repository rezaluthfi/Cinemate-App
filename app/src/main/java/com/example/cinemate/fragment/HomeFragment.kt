package com.example.cinemate.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.cinemate.R
import com.example.cinemate.adapter.LatestMoviesAdapter
import com.example.cinemate.adapter.TrendingMoviesAdapter
import com.example.cinemate.api.RetrofitInstance
import com.example.cinemate.databinding.FragmentHomeBinding
import com.example.cinemate.model.Movie
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.abs
import kotlin.math.max
import android.os.Handler
import android.os.Looper

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var latestMoviesAdapter: LatestMoviesAdapter
    private lateinit var trendingMoviesAdapter: TrendingMoviesAdapter
    private var movieList: List<Movie> = emptyList()
    private lateinit var autoScrollHandler: Handler
    private lateinit var autoScrollRunnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Inisialisasi adapter untuk latest movies
        latestMoviesAdapter = LatestMoviesAdapter(emptyList())
        binding.rvLatestMovies.adapter = latestMoviesAdapter
        binding.rvLatestMovies.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Inisialisasi adapter untuk trending movies
        trendingMoviesAdapter = TrendingMoviesAdapter(emptyList())
        binding.vpTrendingMovies.adapter = trendingMoviesAdapter // Pastikan ini adalah ViewPager2
        binding.vpTrendingMovies.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        // Ambil data film dari API
        getMoviesFromApi()
        setupViewPagerTransition()
        setupAutoScroll()

        return binding.root
    }

    private fun getMoviesFromApi() {
        RetrofitInstance.api.getMovies().enqueue(object : Callback<List<Map<String, Any>>> {
            override fun onResponse(call: Call<List<Map<String, Any>>>, response: Response<List<Map<String, Any>>>) {
                if (response.isSuccessful) {
                    // Log respons mentah untuk debugging
                    Log.d("HomeFragment", "Raw response: ${response.body()}")

                    val movieList = mutableListOf<Movie>()

                    // Mengurai setiap objek dalam array JSON
                    response.body()?.forEach { movieMap ->
                        movieMap.forEach { (key, value) ->
                            if (key != "_id" && value is Map<*, *>) {
                                try {
                                    // Map value menjadi Movie
                                    val movie = parseMovie(value)
                                    movie?.let { movieList.add(it) }
                                } catch (e: Exception) {
                                    Log.e("HomeFragment", "Error parsing movie: ${e.message}")
                                }
                            }
                        }
                    }

                    Log.d("HomeFragment", "Movies fetched: ${movieList.size}")

                    // Filter film trending
                    val trendingMovies = movieList.filter { it.isTrending }
                    Log.d("HomeFragment", "Trending movies: ${trendingMovies.size}")

                    // Set adapter untuk ViewPager
                    trendingMoviesAdapter.updateMovies(trendingMovies)

                    // Set worm dots indicator untuk ViewPager sesuai dengan infinite scrolling
                    //binding.wormDotsIndicatorTrendingMovies.setViewPager2(binding.vpTrendingMovies)

                    // Set adapter untuk RecyclerView
                    latestMoviesAdapter.updateMovies(movieList.filter { !it.isTrending })
                } else {
                    Toast.makeText(context, "Failed to fetch movies", Toast.LENGTH_SHORT).show()
                    Log.e("HomeFragment", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Map<String, Any>>>, t: Throwable) {
                Log.e("HomeFragment", "Error fetching movies: ${t.message}")
                Toast.makeText(context, "Error fetching movies: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Fungsi untuk memetakan objek Map ke kelas Movie
     */
    private fun parseMovie(map: Map<*, *>): Movie? {
        return try {
            Movie(
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                genre = map["genre"] as? String ?: "",
                rating = (map["rating"] as? Number)?.toDouble() ?: 0.0,
                duration = (map["duration"] as? Number)?.toInt() ?: 0,
                posterUrl = map["posterUrl"] as? String ?: "",
                isTrending = map["isTrending"] as? Boolean ?: false,
                isLatest = map["isLatest"] as? Boolean ?: false,
                categories = (map["categories"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                cinemas = (map["cinemas"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            )
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error parsing movie data: ${e.message}")
            null
        }
    }

    private fun setupViewPagerTransition() {
        // Mengatur transisi ViewPager biar keren saat posisi item berubah
        binding.vpTrendingMovies.setPageTransformer { page, position ->
            val absPosition = abs(position)
            page.apply {
                // Skala item berdasarkan posisi
                val scale = max(0.85f, 1 - absPosition)
                scaleX = scale
                scaleY = scale

            }
        }
    }

    private fun setupAutoScroll() {
        autoScrollHandler = Handler(Looper.getMainLooper())
        autoScrollRunnable = object : Runnable {
            override fun run() {
                // Pastikan ada item di adapter sebelum melakukan auto-scroll
                if (trendingMoviesAdapter.itemCount > 0) {
                    val currentItem = binding.vpTrendingMovies.currentItem
                    val nextItem = (currentItem + 1) % trendingMoviesAdapter.itemCount
                    binding.vpTrendingMovies.setCurrentItem(nextItem, true)
                }
                autoScrollHandler.postDelayed(this, 3000) // Ganti slide setiap 3 detik
            }
        }
        autoScrollHandler.postDelayed(autoScrollRunnable, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        autoScrollHandler.removeCallbacks(autoScrollRunnable) // Hentikan auto-scrolling saat view dihancurkan
        _binding = null
    }
}
