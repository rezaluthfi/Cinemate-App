package com.example.cinemate.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TicketHistoryDao {
    @Insert
    suspend fun insert(ticketHistory: TicketHistory)

    @Delete
    suspend fun delete(ticketHistory: TicketHistory) // Metode untuk menghapus riwayat tiket

    @Query("SELECT * FROM ticket_history")
    suspend fun getAllTicketHistories(): List<TicketHistory>
}

