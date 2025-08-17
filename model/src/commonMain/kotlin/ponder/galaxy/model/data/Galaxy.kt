package ponder.galaxy.model.data

import kabinet.db.TableId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Galaxy(
    val galaxyId: GalaxyId,
    val hostId: HostId,
    val name: String,
    val url: String,
    val visibility: Float,
    val createdAt: Instant,
) {
    val intrinsicIndex get() = name.length
}

@JvmInline @Serializable
value class GalaxyId(override val value: String): TableId<String>