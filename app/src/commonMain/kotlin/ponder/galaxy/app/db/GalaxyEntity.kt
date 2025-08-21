package ponder.galaxy.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import ponder.galaxy.model.data.GalaxyId

@Entity
data class GalaxyEntity(
    @PrimaryKey
    val galaxyId: GalaxyId,
    val name: String,
    val url: String,
)