package com.example.cinemate.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TicketDao {
    @Insert
    suspend fun insert(ticket: Ticket)

    @Delete
    suspend fun delete(ticket: Ticket) // Metode untuk menghapus tiket

    @Query("SELECT * FROM tickets")
    suspend fun getAllTickets(): List<Ticket>
}
