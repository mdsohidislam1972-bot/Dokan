package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.AppReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM app_reminders ORDER BY reminderDate ASC, id ASC")
    fun getAllReminders(): Flow<List<AppReminder>>

    @Query("SELECT * FROM app_reminders WHERE isCompleted = :completed ORDER BY reminderDate ASC")
    fun getRemindersByStatus(completed: Boolean): Flow<List<AppReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: AppReminder): Long

    @Update
    suspend fun updateReminder(reminder: AppReminder)

    @Query("UPDATE app_reminders SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean)

    @Delete
    suspend fun deleteReminder(reminder: AppReminder)

    @Query("DELETE FROM app_reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Query("DELETE FROM app_reminders")
    suspend fun clearAll()
}
