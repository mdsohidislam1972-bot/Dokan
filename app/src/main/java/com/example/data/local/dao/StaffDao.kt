package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.StaffMember
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff_members ORDER BY id ASC")
    fun getAllStaff(): Flow<List<StaffMember>>

    @Query("SELECT COUNT(*) FROM staff_members")
    fun getStaffCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffMember): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(staffList: List<StaffMember>)

    @Update
    suspend fun updateStaff(staff: StaffMember)

    @Delete
    suspend fun deleteStaff(staff: StaffMember)

    @Query("DELETE FROM staff_members WHERE id = :id")
    suspend fun deleteStaffById(id: Long)

    @Query("DELETE FROM staff_members")
    suspend fun clearAll()
}
