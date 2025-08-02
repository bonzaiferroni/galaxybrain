package ponder.galaxy.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ponder.galaxy.model.data.GalaxyId

@Dao
interface GalaxyDao {
    @Insert
    suspend fun insert(vararg galaxys: GalaxyEntity): LongArray

    @Update
    suspend fun update(vararg galaxys: GalaxyEntity): Int

    @Delete
    suspend fun delete(vararg galaxys: GalaxyEntity): Int

    @Query("DELETE FROM GalaxyEntity WHERE galaxyId = :galaxyId")
    suspend fun deleteById(galaxyId: GalaxyId): Int
}