package ponder.galaxy.model.data

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