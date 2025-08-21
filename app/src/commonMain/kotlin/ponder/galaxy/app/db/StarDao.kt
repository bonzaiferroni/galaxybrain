package ponder.galaxy.app.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import ponder.galaxy.model.data.StarId

@Dao
interface StarDao {
    @Insert
    suspend fun insert(vararg stars: StarEntity): LongArray

    @Upsert
    suspend fun upsert(vararg stars: StarEntity): LongArray

    @Update
    suspend fun update(vararg stars: StarEntity): Int

    @Delete
    suspend fun delete(vararg stars: StarEntity): Int

    @Query("DELETE FROM StarEntity WHERE starId = :starId")
    suspend fun deleteById(starId: StarId): Int

//    @Query("SELECT * FROM StarEntity WHERE createdAt > :createdAfter ORDER BY visibility DESC LIMIT :limit")
//    suspend fun readVisibleStars(limit: Int, createdAfter: Instant): List<Star>
}