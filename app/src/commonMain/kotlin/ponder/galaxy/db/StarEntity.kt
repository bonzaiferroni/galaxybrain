package ponder.galaxy.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.StarId

@Entity
data class StarEntity(
    @PrimaryKey
    val starId: StarId,
    val galaxyId: GalaxyId,
    val title: String,
    val url: String,
    val visibility: Float,
    val updatedAt: Instant,
    val createdAt: Instant,
    val discoveredAt: Instant,
)