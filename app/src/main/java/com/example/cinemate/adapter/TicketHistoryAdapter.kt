package com.example.cinemate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cinemate.databinding.ItemTicketHistoryBinding
import com.example.cinemate.model.TicketHistory

class TicketHistoryAdapter(
    private val ticketHistories: List<TicketHistory>,
    private val onDeleteTicketHistoryClick: (TicketHistory) -> Unit // Callback untuk tombol hapus riwayat tiket
) : RecyclerView.Adapter<TicketHistoryAdapter.TicketHistoryViewHolder>() {

    class TicketHistoryViewHolder(private val binding: ItemTicketHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ticketHistory: TicketHistory, onDeleteTicketHistoryClick: (TicketHistory) -> Unit) {
            binding.tvMovieTitle.text = ticketHistory.movieTitle
            binding.tvTicketDate.text = ticketHistory.selectedDate
            binding.tvTicketTime.text = ticketHistory.selectedTime
            binding.tvSeat.text = ticketHistory.selectedSeats
            binding.tvCinema.text = ticketHistory.selectedCinema
            binding.tvStatus.text = ticketHistory.status // Tampilkan status tiket

            // Set listener untuk item tiket history
            itemView.setOnLongClickListener {
                onDeleteTicketHistoryClick(ticketHistory) // Panggil callback saat item ditekan lama
                true // Mengindikasikan bahwa event telah ditangani
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketHistoryViewHolder {
        val binding = ItemTicketHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TicketHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TicketHistoryViewHolder, position: Int) {
        holder.bind(ticketHistories[position], onDeleteTicketHistoryClick) // Pass callback ke ViewHolder
    }

    override fun getItemCount(): Int {
        return ticketHistories.size
    }
}
