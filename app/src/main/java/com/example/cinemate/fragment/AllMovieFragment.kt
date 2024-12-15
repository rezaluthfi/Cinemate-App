package com.example.cinemate.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cinemate.R
import com.example.cinemate.adapter.AllMoviesAdapter
import com.example.cinemate.api.RetrofitInstance
import com.example.cinemate.databinding.FragmentAllMovieBinding
import com.example.cinemate.data.model.Movie
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AllMovieFragment : Fragment() {

    private var _binding: FragmentAllMovieBinding? = null
    private val binding get() = _binding!!

    private lateinit var allMoviesAdapter: AllMoviesAdapter
    private var movieList: List<Movie> = emptyList()
    private var filteredMovieList: List<Movie> = emptyList() // Menyimpan daftar film yang difilter
    private var isFiltered: Boolean = false // Menandakan apakah filter diterapkan
    private var filterMenuItem: MenuItem? = null // Menyimpan referensi MenuItem filter
    private var selectedGenre: String? = null // Menyimpan genre yang dipilih
    private var currentSearchQuery: String? = null // Menyimpan query pencarian saat ini

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllMovieBinding.inflate(inflater, container, false)

        // Setup Toolbar
        setupToolbar()

        // Inisialisasi adapter untuk semua film
        allMoviesAdapter = AllMoviesAdapter(emptyList()) { movie ->
            navigateToDetailFragment(movie) // Menangani klik item film
        }
        binding.rvAllMovie.adapter = allMoviesAdapter
        binding.rvAllMovie.layoutManager = LinearLayoutManager(context)

        // Tampilkan ProgressBar saat memuat data
        binding.progressBar.visibility = View.VISIBLE
        binding.tvLabelAllMovie.visibility = View.GONE // Sembunyikan label "All Movies"
        binding.tvNoMovie.visibility = View.GONE // Sembunyikan pesan "Oops! No movies found."
        binding.flGenres.visibility = View.GONE // Sembunyikan genre hingga data dimuat

        // Ambil data film dari API
        getMoviesFromApi()

        return binding.root
    }

    private fun setupToolbar() {
        val toolbar = binding.toolbar
        toolbar.title = "All Movies"
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.icon_back) // Ganti dengan drawable panah kembali Anda
        toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack() // Kembali ke fragment sebelumnya
        }
        setHasOptionsMenu(true) // Mengizinkan fragment untuk menampilkan menu
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
                    allMoviesAdapter.updateMovies(movieList) // Tampilkan semua film
                    updateActiveGenre(this) // Set "All Genre" sebagai aktif
                    selectedGenre = null // Reset genre yang dipilih
                    resetFilter() // Reset filter
                    currentSearchQuery?.let { query -> filterMovies(query) } // Terapkan pencarian jika ada
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
                        selectedGenre = genre // Simpan genre yang dipilih
                        val filteredMovies = movieList.filter { movie ->
                            genre in movie.genre.split(",") // Filter film berdasarkan genre yang dipilih
                        }
                        allMoviesAdapter.updateMovies(filteredMovies) // Tampilkan film yang difilter
                        updateActiveGenre(this) // Set genre ini sebagai aktif
                        applyFilterIcon() // Ubah ikon filter
                        currentSearchQuery?.let { query -> filterMovies(query) } // Terapkan pencarian jika ada
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.all_movie_menu, menu) // Menginflate menu
        filterMenuItem = menu.findItem(R.id.action_filter) // Simpan referensi ke MenuItem filter
        val searchItem = menu.findItem(R.id.action_search)

        // Pastikan actionView diinisialisasi dengan benar
        val searchView = searchItem.actionView as androidx.appcompat.widget.SearchView

        // Mengatur listener untuk pencarian
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText // Simpan query pencarian saat ini
                filterMovies(newText) // Memfilter film berdasarkan input pencarian
                return true
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun filterMovies(query: String?) {
        val moviesToFilter = if (selectedGenre != null) {
            // Jika genre dipilih, filter berdasarkan genre terlebih dahulu
            movieList.filter { movie ->
                selectedGenre in movie.genre.split(",")
            }
        } else {
            movieList // Jika tidak ada genre yang dipilih, gunakan semua film
        }

        if (query.isNullOrEmpty()) {
            allMoviesAdapter.updateMovies(moviesToFilter) // Tampilkan semua film jika query kosong
            binding.tvNoMovie.visibility = View.GONE // Sembunyikan pesan "No Data"
        } else {
            val filteredMovies = moviesToFilter.filter { movie ->
                movie.title.contains(query, ignoreCase = true) // Filter film berdasarkan judul
            }
            allMoviesAdapter.updateMovies(filteredMovies) // Tampilkan film yang difilter

            // Tampilkan pesan jika tidak ada film yang ditemukan
            if (filteredMovies.isEmpty()) {
                binding.tvNoMovie.visibility = View.VISIBLE // Tampilkan pesan "No Data"
            } else {
                binding.tvNoMovie.visibility = View.GONE // Sembunyikan pesan "No Data"
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                // Tidak melakukan apa-apa karena SearchView sudah ditangani di onCreateOptionsMenu
                true
            }
            R.id.action_filter -> {
                showFilterPopup(item) // Tampilkan PopupMenu untuk filter
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showFilterPopup(menuItem: MenuItem) {
        // Gunakan View item dari menu yang diklik sebagai anchor
        val anchorView = (activity as AppCompatActivity).findViewById<View>(menuItem.itemId)

        val popupMenu = PopupMenu(requireContext(), anchorView) // Menggunakan view yang lebih spesifik sebagai anchor
        popupMenu.menuInflater.inflate(R.menu.filter_menu, popupMenu.menu) // Inflate menu filter

        // Atur listener untuk item filter
        popupMenu.setOnMenuItemClickListener { filterItem ->
            when (filterItem.itemId) {
                R.id.filter_name -> {
                    Log.d("Filter", "Filtering by name")
                    applyFilter("name", "alphabet") // Filter berdasarkan nama
                    true
                }
                R.id.filter_rating -> {
                    Log.d("Filter", "Filtering by rating")
                    applyFilter("rating", "high_to_low") // Filter berdasarkan rating
                    true
                }
                R.id.filter_duration -> {
                    Log.d("Filter", "Filtering by duration")
                    applyFilter("duration", "short_to_long") // Filter berdasarkan durasi
                    true
                }
                R.id.filter_reset -> {
                    Log.d("Filter", "Resetting filter")
                    resetFilter() // Reset filter
                    true
                }
                else -> false
            }
        }

        popupMenu.show() // Tampilkan PopupMenu
    }

    private fun getMoviesFromApi() {
        RetrofitInstance.api.getMovies().enqueue(object : Callback<List<Map<String, Any>>> {
            override fun onResponse(call: Call<List<Map<String, Any>>>, response: Response<List<Map<String, Any>>>) {
                // Sembunyikan ProgressBar dan tampilkan RecyclerView
                if (_binding != null) {
                    binding.progressBar.visibility = View.GONE
                    binding.tvNoMovie.visibility = View.GONE // Sembunyikan pesan "No Data"
                    binding.tvLabelAllMovie.visibility = View.VISIBLE // Tampilkan label "All Movies"
                    binding.rvAllMovie.visibility = View.VISIBLE
                    binding.flGenres.visibility = View.VISIBLE // Tampilkan genre setelah data dimuat
                }

                if (response.isSuccessful) {
                    val movieList = mutableListOf<Movie>()

                    response.body()?.forEach { movieMap ->
                        movieMap.forEach { (key, value) ->
                            if (key != "_id" && value is Map<*, *>) {
                                try {
                                    val movie = parseMovie(value)
                                    movie?.let { movieList.add(it) }
                                } catch (e: Exception) {
                                    Log.e("AllMoviesFragment", "Error parsing movie: ${e.message}")
                                }
                            }
                        }
                    }

                    // Update variabel global movieList
                    this@AllMovieFragment.movieList = movieList

                    allMoviesAdapter.updateMovies(movieList)

                    // Setup genres in FlexboxLayout
                    setupGenres(movieList)

                } else {
                    Toast.makeText(context, "Failed to fetch movies", Toast.LENGTH_SHORT).show()
                    Log.e("AllMoviesFragment", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Map<String, Any>>>, t: Throwable) {
                binding.progressBar.visibility = View.GONE // Sembunyikan ProgressBar jika gagal
                Log.e("AllMoviesFragment", "Error fetching movies: ${t.message}")
                Toast.makeText(context, "Error fetching movies: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

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
                cinemas = (map["cinemas"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            )
        } catch (e: Exception) {
            Log.e("AllMoviesFragment", "Error parsing movie data: ${e.message}")
            null
        }
    }

    private fun applyFilter(filterType: String, filterValue: Any) {
        if (movieList.isEmpty()) {
            Toast.makeText(context, "No movies available to filter", Toast.LENGTH_SHORT).show()
            return
        }

        val filteredMovies = when (filterType) {
            "name" -> movieList.sortedBy { it.title } // Filter berdasarkan nama
            "rating" -> movieList.sortedByDescending { it.rating } // Filter berdasarkan rating
            "duration" -> movieList.sortedBy { it.duration } // Filter berdasarkan durasi
            else -> movieList
        }

        // Terapkan filter genre jika ada genre yang dipilih
        val moviesToDisplay = if (selectedGenre != null) {
            filteredMovies.filter { movie ->
                selectedGenre in movie.genre.split(",") // Filter berdasarkan genre yang dipilih
            }
        } else {
            filteredMovies // Jika tidak ada genre yang dipilih, gunakan hasil filter
        }

        allMoviesAdapter.updateMovies(moviesToDisplay)
        isFiltered = true // Menandakan bahwa filter diterapkan
        applyFilterIcon() // Ubah ikon filter
    }

    private fun applyFilterIcon() {
        filterMenuItem?.icon = ContextCompat.getDrawable(requireContext(), R.drawable.icon_filter_reset) // Ganti dengan drawable ikon filter diterapkan
    }

    private fun resetFilter() {
        isFiltered = false // Reset status filter
        allMoviesAdapter.updateMovies(movieList) // Tampilkan semua film
        filterMenuItem?.icon = ContextCompat.getDrawable(requireContext(), R.drawable.icon_filter) // Ganti dengan drawable ikon filter default
        // Kembali ke "All Genre"
        binding.flGenres.removeAllViews() // Menghapus semua tampilan genre yang ada sebelumnya
        setupGenres(movieList) // Setup genre kembali
        selectedGenre = null // Reset genre yang dipilih
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Cek apakah kembali ke HomeFragment atau tidak
        if (requireActivity().supportFragmentManager.backStackEntryCount == 0) {
            // Menampilkan kembali Bottom Navigation Bar
            requireActivity().findViewById<BottomAppBar>(R.id.bottom_app_bar).visibility = View.VISIBLE
            requireActivity().findViewById<FloatingActionButton>(R.id.fab_scan).visibility = View.VISIBLE
        } else {
            // Menyembunyikan Bottom Navigation Bar
            requireActivity().findViewById<BottomAppBar>(R.id.bottom_app_bar).visibility = View.GONE
            requireActivity().findViewById<FloatingActionButton>(R.id.fab_scan).visibility = View.GONE
        }
        _binding = null // Mengatur binding ke null untuk menghindari memory leaks
    }
}
