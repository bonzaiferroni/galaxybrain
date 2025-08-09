package ponder.galaxy.model.data

import kotlinx.serialization.Serializable

@Serializable
data class Chatter(
    val identifier: String,
    val body: String,
    val author: String,
    val visibility: Float,
    val visibilityRatio: Float,
)

@Serializable
data class ChatterDelta(
    val identifier: String,
    val visibility: Float,
    val visibilityRatio: Float,
)

@Serializable
data class ChatterProbe(
    val newChatters: List<Chatter>,
    val deltas: List<ChatterDelta>,
)