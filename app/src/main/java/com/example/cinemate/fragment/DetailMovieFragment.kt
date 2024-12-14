package com.example.cinemate.fragment

import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.cinemate.R
import com.example.cinemate.databinding.Example7CalendarDayBinding
import com.example.cinemate.databinding.FragmentDetailMovieBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class DetailMovieFragment : Fragment() {

    private var _binding: FragmentDetailMovieBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    private var selectedDate = LocalDate.now()
    private var selectedTime: TextView? = null // Variabel untuk menyimpan waktu yang dipilih
    private var selectedSeats = mutableListOf<TextView>() // Menyimpan kursi yang dipilih

    @RequiresApi(Build.VERSION_CODES.O)
    private val dateFormatter = DateTimeFormatter.ofPattern("dd")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailMovieBinding.inflate(inflater, container, false)

        // Memanggil fungsi setupToolbar
        setupToolbar()

        // Ambil data dari argumen
        arguments?.let {
            val title = it.getString("movie_title") ?: "Unknown Title"
            val description = it.getString("movie_description") ?: "No Description"
            val genre = it.getString("movie_genre") ?: "Unknown Genre"
            val rating = it.getDouble("movie_rating", 0.0)
            val duration = it.getInt("movie_duration", 0)
            val posterUrl = it.getString("movie_poster_url") ?: ""
            val cinemas = it.getStringArrayList("movie_cinemas") ?: arrayListOf()

            // Set data ke tampilan
            binding.tvMovieTitle.text = title
            binding.tvMovieDescription.text = description
            binding.tvMovieGenre.text = genre
            binding.tvMovieRating.text = rating.toString()
            binding.tvMovieDuration.text = "${duration} min"

            // Memuat gambar poster film
            Glide.with(this)
                .load(posterUrl)
                .into(binding.ivMoviePoster)

            // Setup dropdown bioskop
            setupCinemaDropdown(cinemas)

            // Setup pilihan jam
            setupTimeOptions()

            // Setup kursi
            setupSeats()
        }

        // Setup kalender untuk memilih tanggal
        setupDateSelection()

        return binding.root
    }

    private fun setupTimeOptions() {
        val timeOptions = listOf("10:00 AM", "12:30 PM", "03:00 PM", "05:30 PM", "08:00 PM") // Contoh pilihan jam
        val flexboxLayout: FlexboxLayout = binding.flTimes

        flexboxLayout.removeAllViews() // Menghapus semua tampilan jam yang ada sebelumnya

        timeOptions.forEach { time ->
            val timeTextView = TextView(requireContext()).apply {
                text = time
                setPadding(32, 16, 32, 16) // Padding kiri, atas, kanan, bawah
                setBackgroundResource(R.drawable.bg_chip) // Pastikan Anda memiliki drawable bg_chip
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white_60)) // Warna teks default
                textSize = 14f // Set ukuran teks
                layoutParams = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 24, 24) // Margin kiri, atas, kanan, bawah
                }
                setOnClickListener {
                    updateActiveTime(this) // Memperbarui waktu aktif
                }
            }
            flexboxLayout.addView(timeTextView) // Menambahkan TextView ke FlexboxLayout
        }
    }

    private fun updateActiveTime(selectedTimeView: TextView) {
        // Reset waktu sebelumnya
        selectedTime?.apply {
            setBackgroundResource(R.drawable.bg_chip) // Background default
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white_60)) // Warna teks default
        }

        // Set waktu aktif
        selectedTime = selectedTimeView.apply {
            setBackgroundResource(R.drawable.bg_chip_active) // Background aktif
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // Warna teks aktif
        }

        // Tampilkan toast dengan waktu yang dipilih
        Toast.makeText(requireContext(), "Selected time: ${selectedTime?.text}", Toast.LENGTH_SHORT).show()
    }

    private fun setupSeats() {
        val leftSeatsContainer: GridLayout = binding.seatsContainerLeft
        val rightSeatsContainer: GridLayout = binding.seatsContainerRight
        leftSeatsContainer.removeAllViews() // Menghapus semua tampilan kursi yang ada sebelumnya
        rightSeatsContainer.removeAllViews() // Menghapus semua tampilan kursi yang ada sebelumnya

        // Contoh kursi yang tersedia dan terisi
        val leftSeats = listOf("A1", "A2", "A3", "A4", "A5", "B1", "B2", "B3", "B4", "B5") // Daftar kursi kiri
        val rightSeats = listOf("C1", "C2", "C3", "C4", "C5", "D1", "D2", "D3", "D4", "D5") // Daftar kursi kanan
        val takenSeats = listOf("A2", "A4", "A5", "B3", "B5", "C1", "C5", "D4") // Contoh kursi yang sudah terisi

        // Menambahkan kursi di sebelah kiri
        leftSeats.forEach { seat ->
            val seatTextView = createSeatTextView(seat, takenSeats)
            leftSeatsContainer.addView(seatTextView) // Menambahkan TextView kursi ke GridLayout kiri
        }

        // Menambahkan kursi di sebelah kanan
        rightSeats.forEach { seat ->
            val seatTextView = createSeatTextView(seat, takenSeats)
            rightSeatsContainer.addView(seatTextView) // Menambahkan TextView kursi ke GridLayout kanan
        }
    }

    private fun createSeatTextView(seat: String, takenSeats: List<String>): TextView {
        return TextView(requireContext()).apply {
            text = seat
            setPadding(16, 8, 16, 8)
            setBackgroundResource(if (takenSeats.contains(seat)) R.drawable.seat_taken else R.drawable.seat_available)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // Warna teks
            textSize = 14f // Set ukuran teks
            layoutParams = GridLayout.LayoutParams().apply {
                width = GridLayout.LayoutParams.WRAP_CONTENT
                height = GridLayout.LayoutParams.WRAP_CONTENT
                setMargins(8, 8, 8, 8) // Margin kiri, atas, kanan, bawah
            }

            setOnClickListener {
                if (takenSeats.contains(seat)) {
                    // Tampilkan toast jika kursi sudah terisi saat diklik
                    Toast.makeText(requireContext(), "Kursi $seat sudah terisi", Toast.LENGTH_SHORT).show()
                } else {
                    toggleSeatSelection(this) // Toggle pemilihan kursi
                }
            }
        }
    }

    private fun toggleSeatSelection(seatTextView: TextView) {
        if (selectedSeats.contains(seatTextView)) {
            // Jika kursi sudah dipilih, hapus dari daftar dan reset tampilan
            selectedSeats.remove(seatTextView)
            seatTextView.setBackgroundResource(R.drawable.seat_available) // Set background kursi tersedia
        } else {
            // Jika kursi belum dipilih, tambahkan ke daftar dan ubah tampilan
            selectedSeats.add(seatTextView)
            seatTextView.setBackgroundResource(R.drawable.seat_selected) // Set background kursi terpilih
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupDateSelection() {
        binding.exSevenCalendar.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer {
                return DayViewContainer(view)
            }

            override fun bind(container: DayViewContainer, data: WeekDay) {
                container.bind(data)
            }
        }

        val currentMonth = YearMonth.now()
        binding.exSevenCalendar.setup(
            currentMonth.minusMonths(5).atStartOfMonth(),
            currentMonth.plusMonths(5).atEndOfMonth(),
            firstDayOfWeekFromLocale()
        )
        binding.exSevenCalendar.scrollToDate(LocalDate.now())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getWeekPageTitle(weekDays: List<WeekDay>): String {
        val startDate = weekDays.first().date
        val endDate = weekDays.last().date
        return "${dateFormatter.format(startDate)} - ${dateFormatter.format(endDate)}"
    }

    private fun setupCinemaDropdown(cinemas: List<String>) {
        val spinner = binding.spinnerCinema
        spinner.setItems(cinemas)
        spinner.setOnSpinnerItemSelectedListener<String> { _, _, _, item ->
            // Simpan bioskop yang dipilih
        }
    }

    private fun setupToolbar() {
        val toolbar = binding.toolbar
        toolbar.title = "Movie Details"
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }

        toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
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

    @RequiresApi(Build.VERSION_CODES.O)
    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val bind = Example7CalendarDayBinding.bind(view)
        lateinit var day: WeekDay

        init {
            view.setOnClickListener {
                if (day.date.isBefore(LocalDate.now())) {
                    // Tanggal sudah lewat, tampilkan toast
                    Toast.makeText(view.context, "Tanggal yang sudah lewat tidak bisa dipilih", Toast.LENGTH_SHORT).show()
                } else if (selectedDate != day.date) {
                    val oldDate = selectedDate
                    selectedDate = day.date
                    binding.exSevenCalendar.notifyDateChanged(day.date)
                    oldDate?.let { binding.exSevenCalendar.notifyDateChanged(it) }
                }
            }
        }

        fun bind(day: WeekDay) {
            this.day = day
            bind.exSevenDateText.text = dateFormatter.format(day.date)
            bind.exSevenDayText.text = day.date.dayOfWeek.name.take(3)

            val colorRes = when {
                day.date.isBefore(LocalDate.now()) -> R.color.gray // Warna untuk tanggal yang sudah lewat
                day.date == selectedDate -> R.color.primary // Warna untuk tanggal yang dipilih
                else -> R.color.white // Warna untuk tanggal yang tidak dipilih
            }
            bind.exSevenDateText.setTextColor(view.context.getColor(colorRes))
            bind.exSevenSelectedView.isVisible = day.date == selectedDate
        }
    }
}
