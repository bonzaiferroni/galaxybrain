package ponder.galaxy.model.data

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Chatter(
    val identifier: String,
    val body: String,
    val author: String,
    val permalink: String,
    val depth: Int?,
    val visibility: Float,
    val visibilityRatio: Float,
    val createdAt: Instant,
) {
    fun getRise(now: Instant, riseFactor: Int) = calculateRise(now - createdAt, visibilityRatio, riseFactor)
}

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