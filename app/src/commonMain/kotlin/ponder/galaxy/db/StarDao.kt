package ponder.galaxy.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ponder.galaxy.model.data.StarId

@Dao
interface StarDao {
    @Insert
    suspend fun insert(vararg stars: StarEntity): LongArray

    @Update
    suspend fun update(vararg stars: StarEntity): Int

    @Delete
    suspend fun delete(vararg stars: StarEntity): Int

    @Query("DELETE FROM StarEntity WHERE starId = :starId")
    suspend fun deleteById(starId: StarId): Int
}