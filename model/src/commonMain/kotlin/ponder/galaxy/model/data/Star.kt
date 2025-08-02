package ponder.galaxy.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Star(
    val starId: StarId,
    val galaxyId: GalaxyId,
    val title: String,
    val url: String,
)

@JvmInline @Serializable
value class StarId(val value: String)