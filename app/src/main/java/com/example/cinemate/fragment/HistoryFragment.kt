package com.example.cinemate.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cinemate.R
import com.example.cinemate.adapter.TicketHistoryAdapter
import com.example.cinemate.databinding.FragmentHistoryBinding
import com.example.cinemate.data.model.AppDatabase
import com.example.cinemate.data.model.TicketHistory
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var ticketHistoryAdapter: TicketHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        // Setup RecyclerView
        binding.rvTicketHistory.layoutManager = LinearLayoutManager(requireContext())
        ticketHistoryAdapter = TicketHistoryAdapter(emptyList()) { ticketHistory ->
            showDeleteConfirmation(ticketHistory) // Callback untuk tombol hapus riwayat tiket
        }
        binding.rvTicketHistory.adapter = ticketHistoryAdapter

        loadTicketHistories()

        return binding.root
    }

    private fun loadTicketHistories() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val ticketHistories = db.ticketHistoryDao().getAllTicketHistories()
            // Cek apakah daftar riwayat tiket kosong
            if (ticketHistories.isEmpty()) {
                binding.tvNoHistoryTicket.visibility = View.VISIBLE // Tampilkan TextView jika kosong
                binding.rvTicketHistory.visibility = View.GONE // Sembunyikan RecyclerView
            } else {
                binding.tvNoHistoryTicket.visibility = View.GONE // Sembunyikan TextView jika tidak kosong
                binding.rvTicketHistory.visibility = View.VISIBLE // Tampilkan RecyclerView
                ticketHistoryAdapter = TicketHistoryAdapter(ticketHistories) { ticketHistory ->
                    showDeleteConfirmation(ticketHistory) // Callback untuk tombol hapus riwayat tiket
                }
                binding.rvTicketHistory.adapter = ticketHistoryAdapter
            }
        }
    }

    private fun showDeleteConfirmation(ticketHistory: TicketHistory) {
        // Inflate layout dialog
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_delete_history_ticket, null)

        // Create dialog
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Set up dialog buttons
        val btnYes: Button = dialogView.findViewById(R.id.btn_yes)
        val btnCancel: TextView = dialogView.findViewById(R.id.btn_cancel)

        btnYes.setOnClickListener {
            deleteTicketHistory(ticketHistory) // Hapus riwayat tiket jika pengguna mengonfirmasi
            dialog.dismiss() // Tutup dialog
        }

        btnCancel.setOnClickListener {
            dialog.dismiss() // Tutup dialog jika pengguna membatalkan
        }

        dialog.show() // Tampilkan dialog
    }

    private fun deleteTicketHistory(ticketHistory: TicketHistory) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            db.ticketHistoryDao().delete(ticketHistory) // Panggil metode delete dari DAO
            Toast.makeText(requireContext(), "Riwayat tiket berhasil dihapus!", Toast.LENGTH_SHORT).show()
            loadTicketHistories() // Memuat ulang daftar riwayat tiket setelah penghapusan
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Mengatur binding ke null untuk menghindari memory leaks
    }
}
