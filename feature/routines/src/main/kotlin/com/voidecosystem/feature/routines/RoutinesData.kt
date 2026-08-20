package com.voidecosystem.feature.routines

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "routine_schedules")
data class RoutineSchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val isEnabled: Boolean = true,
) {
    /** Minutes since midnight, for simple range comparisons. */
    val startMinutesOfDay: Int get() = startHour * 60 + startMinute
    val endMinutesOfDay: Int get() = endHour * 60 + endMinute
}

@Dao
interface RoutineScheduleDao {
    @Query("SELECT * FROM routine_schedules ORDER BY startHour, startMinute")
    fun observeAll(): Flow<List<RoutineSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(schedule: RoutineSchedule)

    @Update
    suspend fun update(schedule: RoutineSchedule)

    @Query("DELETE FROM routine_schedules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM routine_schedules WHERE isEnabled = 1")
    suspend fun getEnabled(): List<RoutineSchedule>
}

@Database(entities = [RoutineSchedule::class], version = 1, exportSchema = false)
abstract class RoutinesDatabase : RoomDatabase() {
    abstract fun scheduleDao(): RoutineScheduleDao

    companion object {
        @Volatile private var instance: RoutinesDatabase? = null

        fun getInstance(context: Context): RoutinesDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RoutinesDatabase::class.java,
                    "void-routines.db",
                ).build().also { instance = it }
            }
    }
}
