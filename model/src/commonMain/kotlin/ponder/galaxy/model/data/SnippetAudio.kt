package ponder.galaxy.model.data

import kotlinx.serialization.Serializable

@Serializable
data class SnippetAudio(
    val snippetId: SnippetId,
    val path: String,
)