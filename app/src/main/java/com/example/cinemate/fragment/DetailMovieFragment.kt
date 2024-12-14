package com.example.cinemate.fragment

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.cinemate.R
import com.example.cinemate.databinding.Example7CalendarDayBinding
import com.example.cinemate.databinding.FragmentDetailMovieBinding
import com.example.cinemate.model.AppDatabase
import com.example.cinemate.model.Ticket
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private var bookedSeats = mutableListOf<String>() // Menyimpan kursi yang sudah dipesan

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

            // Ambil kursi yang sudah dipesan
            loadBookedSeats()

            // Setup kursi
            setupSeats()
        }

        // Setup kalender untuk memilih tanggal
        setupDateSelection()

        // Setup tombol untuk mendapatkan tiket
        setupGetTicketButton()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadBookedSeats() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val tickets = db.ticketDao().getAllTickets()
            bookedSeats.clear()

            val movieTitle = binding.tvMovieTitle.text.toString()
            val selectedDateString = selectedDate.toString()
            val selectedTimeString = selectedTime?.text.toString()
            val selectedCinemaString = binding.spinnerCinema.text.toString()

            tickets.forEach { ticket ->
                // Pastikan semua kondisi pencocokan presisi
                if (ticket.movieTitle.trim() == movieTitle.trim() &&
                    ticket.selectedDate.trim() == selectedDateString.trim() &&
                    ticket.selectedTime.trim() == selectedTimeString.trim() &&
                    ticket.selectedCinema.trim() == selectedCinemaString.trim()) {

                    bookedSeats.addAll(ticket.selectedSeats.split(", "))
                }
            }

            // Setup tampilan kursi
            setupSeats()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupGetTicketButton() {
        val btnGetTicket: Button = binding.btnGetTicket
        btnGetTicket.setOnClickListener {
            // Validasi apakah semua data sudah diisi
            if (selectedDate == null || selectedTime == null || selectedSeats.isEmpty()) {
                Toast.makeText(requireContext(), "Silakan pilih tanggal, waktu, dan kursi terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validasi apakah bioskop sudah dipilih
            val selectedCinema = binding.spinnerCinema.text.toString()
            if (selectedCinema.isEmpty()) {
                Toast.makeText(requireContext(), "Silakan pilih bioskop terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val movieTitle = binding.tvMovieTitle.text.toString()
            val selectedDateString = selectedDate.toString() // Format sesuai kebutuhan
            val selectedTimeString = selectedTime?.text.toString()
            val selectedSeatsString = selectedSeats.joinToString(", ") { it.text.toString() }

            // Buat objek tiket dengan status "Dipesan"
            val ticket = Ticket(
                movieTitle = movieTitle,
                selectedDate = selectedDateString,
                selectedTime = selectedTimeString,
                selectedSeats = selectedSeatsString,
                selectedCinema = selectedCinema,
                status = "Belum Dicetak" // Set status tiket
            )

            // Cek apakah kursi yang dipilih sudah dipesan
            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(requireContext())
                val existingTickets = db.ticketDao().getAllTickets() // Ambil semua tiket
                val bookedSeats = existingTickets.flatMap {
                    if (it.movieTitle == movieTitle && it.selectedDate == selectedDateString && it.selectedTime == selectedTimeString && it.selectedCinema == selectedCinema) {
                        it.selectedSeats.split(", ") // Ambil semua kursi yang sudah dipesan untuk film yang sama, tanggal, waktu, dan bioskop
                    } else {
                        emptyList() // Jika film, tanggal, waktu, atau bioskop tidak sama, kembalikan daftar kosong
                    }
                }

                // Cek apakah ada kursi yang dipilih sudah dipesan
                val alreadyBookedSeats = selectedSeats.map { it.text.toString() }.filter { bookedSeats.contains(it) }

                if (alreadyBookedSeats.isNotEmpty()) {
                    Toast.makeText(requireContext(), "Kursi ${alreadyBookedSeats.joinToString(", ")} sudah dipesan sebelumnya!", Toast.LENGTH_SHORT).show()
                } else {
                    // Simpan tiket ke database
                    db.ticketDao().insert(ticket)
                    Toast.makeText(requireContext(), "Tiket berhasil dipesan!", Toast.LENGTH_SHORT).show()
                }
            }
        }
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

        // Contoh kursi yang tersedia
        val leftSeats = listOf("A1", "A2", "A3", "A4", "A5", "B1", "B2", "B3", "B4", "B5") // Daftar kursi kiri
        val rightSeats = listOf("C1", "C2", "C3", "C4", "C5", "D1", "D2", "D3", "D4", "D5") // Daftar kursi kanan

        // Menambahkan kursi di sebelah kiri
        leftSeats.forEach { seat ->
            val seatTextView = createSeatTextView(seat)
            leftSeatsContainer.addView(seatTextView) // Menambahkan TextView kursi ke GridLayout kiri
        }

        // Menambahkan kursi di sebelah kanan
        rightSeats.forEach { seat ->
            val seatTextView = createSeatTextView(seat)
            rightSeatsContainer.addView(seatTextView) // Menambahkan TextView kursi ke GridLayout kanan
        }
    }

    private fun createSeatTextView(seat: String): TextView {
        return TextView(requireContext()).apply {
            text = seat
            setPadding(16, 8, 16, 8)
            // Set background berdasarkan status kursi (tersedia atau terisi)
            // Check resource IDs and log values for debugging
            Log.d("DetailMovieFragment", "Booked seats: $bookedSeats")
            Log.d("DetailMovieFragment", "Current seat: $seat")

            if (bookedSeats.contains(seat)) {
                Log.d("DetailMovieFragment", "Seat $seat is booked.")
                setBackgroundResource(R.drawable.seat_booked) // Set background kursi terisi
            } else {
                Log.d("DetailMovieFragment", "Seat $seat is available.")
                setBackgroundResource(R.drawable.seat_available) // Set background kursi tersedia
            }
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // Warna teks
            textSize = 14f // Set ukuran teks
            layoutParams = GridLayout.LayoutParams().apply {
                width = GridLayout.LayoutParams.WRAP_CONTENT
                height = GridLayout.LayoutParams.WRAP_CONTENT
                setMargins(8, 8, 8, 8) // Margin kiri, atas, kanan, bawah
            }

            setOnClickListener {
                if (bookedSeats.contains(seat)) {
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
