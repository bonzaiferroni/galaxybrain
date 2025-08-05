package ponder.galaxy.model.data

import kabinet.db.TableId
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Galaxy(
    val galaxyId: GalaxyId,
    val name: String,
    val url: String,
)

@JvmInline @Serializable
value class GalaxyId(override val value: String): TableId<String>