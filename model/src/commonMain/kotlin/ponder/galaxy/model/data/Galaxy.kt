package ponder.galaxy.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Galaxy(
    val galaxyId: GalaxyId,
    val name: String,
    val url: String,
)

@JvmInline @Serializable
value class GalaxyId(val value: String)