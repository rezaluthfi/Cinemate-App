package com.example.cinemate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cinemate.databinding.ItemTicketBinding
import com.example.cinemate.model.Ticket

class TicketAdapter(
    private val tickets: List<Ticket>,
    private val onPrintTicketClick: (Ticket) -> Unit,
    private val onDeleteTicketClick: (Ticket) -> Unit // Callback untuk tombol hapus tiket
) : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    class TicketViewHolder(private val binding: ItemTicketBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ticket: Ticket, onPrintTicketClick: (Ticket) -> Unit, onDeleteTicketClick: (Ticket) -> Unit) {
            with(binding) {
                tvMovieTitle.text = ticket.movieTitle
                tvTicketDate.text = ticket.selectedDate
                tvTicketTime.text = ticket.selectedTime
                tvSeat.text = ticket.selectedSeats
                tvCinema.text = ticket.selectedCinema
                tvStatus.text = ticket.status // Tampilkan status tiket

                // Set listener untuk tombol cetak tiket
                btnPrintTicket.setOnClickListener {
                    onPrintTicketClick(ticket) // Panggil callback saat tombol ditekan
                }

                // Set listener untuk tombol hapus tiket
                btnCancelTicket.setOnClickListener {
                    onDeleteTicketClick(ticket) // Panggil callback saat tombol hapus ditekan
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val binding = ItemTicketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TicketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        holder.bind(tickets[position], onPrintTicketClick, onDeleteTicketClick) // Pass callback ke ViewHolder
    }

    override fun getItemCount(): Int {
        return tickets.size
    }
}
