package ponder.galaxy.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.StarId

@Entity
data class StarEntity(
    @PrimaryKey
    val starId: StarId,
    val galaxyId: GalaxyId,
    val title: String,
    val url: String,
)