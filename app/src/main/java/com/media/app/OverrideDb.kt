package com.media.app

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// One row per user-edited file. Keyed by the MediaStore id.
@Entity(tableName = "overrides")
data class MediaOverride(
    @PrimaryKey val mediaId: Long,
    val customTitle: String? = null,
    val customArtist: String? = null,   // artist / host / author depending on pillar
    val details: String? = null,
    val pillar: String? = null          // "MUSIC" | "PODCAST" | "AUDIOBOOK" | null
)

@Dao
interface OverrideDao {
    @Query("SELECT * FROM overrides")
    fun observeAll(): Flow<List<MediaOverride>>

    @Query("SELECT * FROM overrides")
    suspend fun getAll(): List<MediaOverride>

    @Upsert
    suspend fun upsert(override: MediaOverride)

    @Query("DELETE FROM overrides WHERE mediaId = :id")
    suspend fun delete(id: Long)
}

@Database(entities = [MediaOverride::class], version = 1, exportSchema = false)
abstract class OverrideDatabase : RoomDatabase() {
    abstract fun dao(): OverrideDao

    companion object {
        @Volatile private var INSTANCE: OverrideDatabase? = null

        fun get(context: Context): OverrideDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    OverrideDatabase::class.java,
                    "media_overrides.db"
                ).build().also { INSTANCE = it }
            }
    }
}
