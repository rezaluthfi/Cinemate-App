package com.example.cinemate.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cinemate.R
import com.example.cinemate.adapter.TicketAdapter
import com.example.cinemate.databinding.FragmentTicketBinding
import com.example.cinemate.data.model.AppDatabase
import com.example.cinemate.data.model.Ticket
import com.example.cinemate.data.model.TicketHistory
import kotlinx.coroutines.launch

class TicketFragment : Fragment() {

    private var _binding: FragmentTicketBinding? = null
    private val binding get() = _binding!!

    private lateinit var ticketAdapter: TicketAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTicketBinding.inflate(inflater, container, false)

        // Setup RecyclerView
        binding.rvTicket.layoutManager = LinearLayoutManager(requireContext())
        ticketAdapter = TicketAdapter(emptyList(), { ticket ->
            printTicket(ticket) // Callback untuk tombol cetak tiket
        }, { ticket ->
            showDeleteConfirmation(ticket) // Callback untuk tombol hapus tiket
        })
        binding.rvTicket.adapter = ticketAdapter

        loadTickets()

        return binding.root
    }

    private fun loadTickets() {
        binding.progressBar.visibility = View.VISIBLE // Tampilkan ProgressBar saat loading
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val tickets = db.ticketDao().getAllTickets() // Ambil semua tiket
            // Filter tiket yang belum dicetak
            val unprintedTickets = tickets.filter { it.status != "Dicetak" }
            ticketAdapter = TicketAdapter(unprintedTickets, { ticket ->
                printTicket(ticket) // Callback untuk tombol cetak tiket
            }, { ticket ->
                showDeleteConfirmation(ticket) // Callback untuk tombol hapus tiket
            })
            binding.rvTicket.adapter = ticketAdapter
            binding.progressBar.visibility = View.GONE // Sembunyikan ProgressBar setelah loading selesai

            // Tampilkan atau sembunyikan pesan tidak ada tiket
            if (unprintedTickets.isEmpty()) {
                binding.tvNoTicket.visibility = View.VISIBLE // Tampilkan pesan tidak ada tiket
            } else {
                binding.tvNoTicket.visibility = View.GONE // Sembunyikan pesan tidak ada tiket
            }
        }
    }

    private fun showDeleteConfirmation(ticket: Ticket) {
        // Inflate layout dialog
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_cancel_ticket, null)

        // Create dialog
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Set up dialog buttons
        val btnYes: Button = dialogView.findViewById(R.id.btn_yes)
        val btnCancel: TextView = dialogView.findViewById(R.id.btn_cancel)

        btnYes.setOnClickListener {
            deleteTicket(ticket) // Hapus tiket jika pengguna mengonfirmasi
            dialog.dismiss() // Tutup dialog
        }

        btnCancel.setOnClickListener {
            dialog.dismiss() // Tutup dialog jika pengguna membatalkan
        }

        dialog.show() // Tampilkan dialog
    }

    private fun deleteTicket(ticket: Ticket) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            db.ticketDao().delete(ticket) // Panggil metode delete dari DAO
            Toast.makeText(requireContext(), "Tiket berhasil dibatalkan!", Toast.LENGTH_SHORT).show()
            loadTickets() // Memuat ulang daftar tiket setelah penghapusan
        }
    }

    private fun printTicket(ticket: Ticket) {
        // Buat objek tiket history
        val ticketHistory = TicketHistory(
            movieTitle = ticket.movieTitle,
            selectedDate = ticket.selectedDate,
            selectedTime = ticket.selectedTime,
            selectedSeats = ticket.selectedSeats,
            selectedCinema = ticket.selectedCinema,
            status = "Dicetak" // Status tiket
        )

        // Simpan tiket ke history dan hapus dari tiket
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            db.ticketHistoryDao().insert(ticketHistory) // Simpan ke history
            db.ticketDao().delete(ticket) // Hapus dari tiket
            Toast.makeText(requireContext(), "Tiket berhasil dicetak!", Toast.LENGTH_SHORT).show()
            loadTickets() // Memuat ulang daftar tiket setelah penghapusan
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Mengatur binding ke null untuk menghindari memory leaks
    }
}
