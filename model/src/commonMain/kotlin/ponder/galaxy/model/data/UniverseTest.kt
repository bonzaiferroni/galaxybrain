package ponder.galaxy.model.data

import kotlinx.serialization.Serializable

@Serializable
data class UniverseTest(
    val distance: Float,
    val snippet: Snippet,
)