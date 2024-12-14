package com.example.cinemate.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ticket_history")
data class TicketHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val movieTitle: String,
    val selectedDate: String,
    val selectedTime: String,
    val selectedSeats: String,
    val selectedCinema: String,
    val status: String // Status tiket
)
