package com.voidecosystem.feature.pantry

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
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "pantry_items")
data class PantryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val rating: Int = 3,
    val flavorNotes: String = "",
    val dietaryTags: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Dao
interface PantryDao {
    @Query("SELECT * FROM pantry_items ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PantryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: PantryItem)

    @Query("DELETE FROM pantry_items WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Database(entities = [PantryItem::class], version = 1, exportSchema = false)
abstract class PantryDatabase : RoomDatabase() {
    abstract fun pantryDao(): PantryDao

    companion object {
        @Volatile private var instance: PantryDatabase? = null

        fun getInstance(context: Context): PantryDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PantryDatabase::class.java,
                    "void-pantry.db",
                ).build().also { instance = it }
            }
    }
}
