package com.statusmaker.videoapp.data.db

import android.content.Context
import androidx.room.*
import com.statusmaker.videoapp.data.model.FavoriteTemplate
import com.statusmaker.videoapp.data.model.Project
import kotlinx.coroutines.flow.Flow

// ─── DAO ──────────────────────────────────────────────────────────────────────

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getById(id: Long): Project?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: Project): Long

    @Update
    suspend fun update(project: Project)

    @Delete
    suspend fun delete(project: Project)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun getProjectCount(): Int
}

@Dao
interface FavoriteDao {
    @Query("SELECT templateId FROM favorite_templates ORDER BY addedAt DESC")
    fun getAllFavoriteIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_templates WHERE templateId = :templateId)")
    fun isFavorite(templateId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(favorite: FavoriteTemplate)

    @Query("DELETE FROM favorite_templates WHERE templateId = :templateId")
    suspend fun remove(templateId: String)

    @Query("SELECT COUNT(*) FROM favorite_templates")
    suspend fun count(): Int
}

// ─── Database ─────────────────────────────────────────────────────────────────

@Database(
    entities = [Project::class, FavoriteTemplate::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "status_maker_db"
                ).fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
