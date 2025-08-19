package ponder.galaxy.model.data

import kotlinx.serialization.Serializable

@Serializable
data class NewStarContent(
    val starId: StarId,
    val content: String,
    val isHtml: Boolean,
)