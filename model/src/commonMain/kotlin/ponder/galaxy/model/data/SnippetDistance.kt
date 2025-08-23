package ponder.galaxy.model.data

import kotlinx.serialization.Serializable

@Serializable
data class SnippetDistance(
    val distance: Float,
    val snippet: Snippet,
)