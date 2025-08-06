package ponder.galaxy.model.data

import kabinet.db.TableId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Star(
    val starId: StarId,
    val galaxyId: GalaxyId,
    val title: String,
    val link: String,
    val permalink: String,
    val thumbnailUrl: String?,
    val visibility: Float,
    val commentCount: Int,
    val voteCount: Int,
    val updatedAt: Instant,
    val createdAt: Instant,
    val discoveredAt: Instant,
)

@JvmInline @Serializable
value class StarId(override val value: String): TableId<String>
