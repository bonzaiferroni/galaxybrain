package ponder.galaxy.model.data

import kabinet.db.TableId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class StarLink(
    val starLinkId: StarLinkId,
    val fromStarId: StarId?,
    val toStarId: StarId?,
    val url: String,
    val createdAt: Instant,
)

@JvmInline @Serializable
value class StarLinkId(override val value: String): TableId<String>