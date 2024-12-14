package com.example.cinemate.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
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
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.cinemate.adapter.GenreMoviesAdapter
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var latestMoviesAdapter: LatestMoviesAdapter
    private lateinit var trendingMoviesAdapter: TrendingMoviesAdapter
    private lateinit var genreMoviesAdapter: GenreMoviesAdapter
    private var movieList: List<Movie> = emptyList()
    private lateinit var viewPager2: ViewPager2
    private lateinit var handler: Handler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Inisialisasi adapter untuk latest movies
        latestMoviesAdapter = LatestMoviesAdapter(emptyList()) { movie ->
            navigateToDetailFragment(movie) // Menangani klik item film
        }
        binding.rvLatestMovies.adapter = latestMoviesAdapter
        binding.rvLatestMovies.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Inisialisasi adapter untuk Genre Movies
        genreMoviesAdapter = GenreMoviesAdapter(emptyList()) { movie ->
            navigateToDetailFragment(movie) // Menangani klik item film
        }
        binding.rvGenreMovies.adapter = genreMoviesAdapter
        binding.rvGenreMovies.layoutManager = LinearLayoutManager(context)

        // Inisialisasi adapter untuk trending movies
        setupViewPager()
        setupTransformer()

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 3000)
            }
        })

        // Ambil data film dari API
        getMoviesFromApi()

        // After
        binding.tvViewAllGenres.setOnClickListener {
            navigateToAllMoviesFragment()
        }

        binding.tvViewAllLatest.setOnClickListener {
            navigateToAllMoviesFragment()
        }

        return binding.root
    }

    private fun navigateToAllMoviesFragment() {
        val fragment = AllMovieFragment()

        // Menghilangkan Bottom Navigation Bar
        requireActivity().findViewById<BottomAppBar>(R.id.bottom_app_bar).visibility = View.GONE
        requireActivity().findViewById<FloatingActionButton>(R.id.fab_scan).visibility = View.GONE

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, 3000)
    }

    private val runnable = Runnable {
        viewPager2.currentItem += 1
    }

    private fun setupTransformer() {
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(40))
        transformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }
        viewPager2.setPageTransformer(transformer)
    }

    private fun setupViewPager() {
        handler = Handler(Looper.myLooper()!!)
        viewPager2 = binding.vpTrendingMovies
        trendingMoviesAdapter = TrendingMoviesAdapter(emptyList()) { movie ->
            navigateToDetailFragment(movie) // Menangani klik item film
        }
        viewPager2.adapter = trendingMoviesAdapter

        viewPager2.offscreenPageLimit = 3
        viewPager2.clipToPadding = false
        viewPager2.clipChildren = false
        viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    }

    private fun getMoviesFromApi() {
        RetrofitInstance.api.getMovies().enqueue(object : Callback<List<Map<String, Any>>> {
            override fun onResponse(call: Call<List<Map<String, Any>>>, response: Response<List<Map<String, Any>>>) {
                if (response.isSuccessful) {
                    Log.d("HomeFragment", "Raw response: ${response.body()}")

                    val movieList = mutableListOf<Movie>()

                    response.body()?.forEach { movieMap ->
                        movieMap.forEach { (key, value) ->
                            if (key != "_id" && value is Map<*, *>) {
                                try {
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

                    // Set adapter untuk RecyclerView
                    genreMoviesAdapter.updateMovies(movieList)

                    // Set adapter untuk RecyclerView
                    latestMoviesAdapter.updateMovies(movieList.filter { !it.isTrending })

                    // Setup genres in FlexboxLayout
                    setupGenres(movieList)
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

    private fun setupGenres(movieList: List<Movie>) {
        // Pastikan binding tidak null
        _binding?.let { binding ->
            // Mengambil semua genre yang unik dan mengurutkannya secara alfabet
            val genres = movieList.flatMap { it.genre.split(",") }.distinct().sorted()
            binding.flGenres.removeAllViews() // Menghapus semua tampilan genre yang ada sebelumnya

            // Variabel untuk melacak genre aktif
            var activeGenre: TextView? = null

            // Fungsi untuk memperbarui tampilan genre
            fun updateActiveGenre(selectedGenre: TextView) {
                // Reset genre sebelumnya
                activeGenre?.apply {
                    setBackgroundResource(R.drawable.bg_chip) // Background default
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white_60)) // Warna teks default
                }

                // Set genre aktif
                selectedGenre.apply {
                    setBackgroundResource(R.drawable.bg_chip_active) // Background aktif
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // Warna teks aktif
                }

                activeGenre = selectedGenre
            }

            // Menambahkan "All Genre" sebagai pilihan default
            val allGenreTextView = TextView(requireContext()).apply {
                text = "All Genre"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white_60))
                setBackgroundResource(R.drawable.bg_chip) // Background default
                setPadding(32, 16, 32, 16) // Padding kiri, atas, kanan, bawah
                layoutParams = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 24, 24) // Margin kiri, atas, kanan, bawah
                }
                setOnClickListener {
                    genreMoviesAdapter.updateMovies(movieList) // Tampilkan semua film
                    updateActiveGenre(this) // Set "All Genre" sebagai aktif
                }
            }
            binding.flGenres.addView(allGenreTextView) // Menambahkan "All Genre" ke FlexboxLayout
            updateActiveGenre(allGenreTextView) // Set "All Genre" sebagai aktif default

            // Menambahkan genre lainnya
            genres.forEach { genre ->
                val genreTextView = TextView(requireContext()).apply {
                    text = genre
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white_60))
                    setBackgroundResource(R.drawable.bg_chip) // Background default
                    setPadding(32, 16, 32, 16) // Padding kiri, atas, kanan, bawah
                    layoutParams = FlexboxLayout.LayoutParams(
                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                        FlexboxLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 24, 24) // Margin kiri, atas, kanan, bawah
                    }
                    setOnClickListener {
                        val filteredMovies = movieList.filter { movie ->
                            genre in movie.genre.split(",") // Filter film berdasarkan genre yang dipilih
                        }
                        genreMoviesAdapter.updateMovies(filteredMovies) // Tampilkan film yang difilter
                        updateActiveGenre(this) // Set genre ini sebagai aktif
                    }
                }
                binding.flGenres.addView(genreTextView) // Menambahkan genre ke FlexboxLayout
            }
        }
    }

    private fun navigateToDetailFragment(movie: Movie) {
        val detailFragment = DetailMovieFragment().apply {
            arguments = Bundle().apply {
                putString("movie_title", movie.title)
                putString("movie_description", movie.description)
                putString("movie_genre", movie.genre)
                putDouble("movie_rating", movie.rating)
                putInt("movie_duration", movie.duration)
                putString("movie_poster_url", movie.posterUrl)
                putStringArrayList("movie_cinemas", ArrayList(movie.cinemas))
            }
        }
        // Menyembunyikan Bottom Navigation Bar
        requireActivity().findViewById<BottomAppBar>(R.id.bottom_app_bar).visibility = View.GONE
        requireActivity().findViewById<FloatingActionButton>(R.id.fab_scan).visibility = View.GONE

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun parseMovie(map: Map<*, *>): Movie? {
        return try {
            Movie(
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                genre = map["genre"] as? String ?: "", // Menggunakan genre
                rating = (map["rating"] as? Number)?.toDouble() ?: 0.0,
                duration = (map["duration"] as? Number)?.toInt() ?: 0,
                posterUrl = map["posterUrl"] as? String ?: "",
                isTrending = map["isTrending"] as? Boolean ?: false,
                isLatest = map["isLatest"] as? Boolean ?: false,
                cinemas = (map["cinemas"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            )
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error parsing movie data: ${e.message}")
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Mengatur binding ke null untuk menghindari memory leaks
        _binding = null
    }
}
