package ponder.galaxy.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId

@Entity(
//    foreignKeys = [
//        ForeignKey(GalaxyEntity::class, ["galaxyId"], ["galaxyId"], ForeignKey.CASCADE)
//    ],
//    indices = [
//        Index("galaxyId")
//    ]
)
data class StarEntity(
    @PrimaryKey
    val starId: StarId,
    // val galaxyId: GalaxyId,
    val title: String?,
    val url: String,
    val visibility: Float?,
    val updatedAt: Instant,
    val createdAt: Instant,
    // val discoveredAt: Instant,
)

fun Star.toEntity() = StarEntity(
    starId = starId,
    // galaxyId = galaxyId,
    title = title,
    url = url,
    visibility = visibility,
    updatedAt = updatedAt,
    createdAt = createdAt,
    // discoveredAt = accessedAt
)