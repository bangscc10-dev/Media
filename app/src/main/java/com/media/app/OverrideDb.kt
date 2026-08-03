package com.media.app

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

@Entity(tableName = "play_history")
data class PlayHistory(
    @PrimaryKey val mediaId: Long,
    val lastPlayed: Long   // epoch millis
)

@Dao
interface HistoryDao {
    @Upsert
    suspend fun record(entry: PlayHistory)

    @Query("SELECT * FROM play_history ORDER BY lastPlayed DESC LIMIT :limit")
    fun observeRecent(limit: Int): kotlinx.coroutines.flow.Flow<List<PlayHistory>>

    @Query("DELETE FROM play_history")
    suspend fun clear()
}

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

@Database(entities = [MediaOverride::class, PlayHistory::class], version = 2, exportSchema = false)
abstract class OverrideDatabase : RoomDatabase() {
    abstract fun dao(): OverrideDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile private var INSTANCE: OverrideDatabase? = null

        // v1 -> v2: add play_history table, preserve all existing overrides.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `play_history` " +
                    "(`mediaId` INTEGER NOT NULL, `lastPlayed` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`mediaId`))"
                )
            }
        }

        fun get(context: Context): OverrideDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    OverrideDatabase::class.java,
                    "media_overrides.db"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
    }
}
