package ponder.galaxy.model.reddit

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
data class RedditData<T>(
    val kind: String,
    val data: T
)

typealias RedditId = String

@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("kind")
@Serializable
sealed interface RedditDtoBox {
    val data: RedditDto
}

@Serializable
@SerialName("Listing")
data class RedditListingBox<T: RedditDtoBox>(
    override val data: RedditListingDto<T>
): RedditDtoBox

@Serializable
@SerialName("t1")
data class RedditCommentBox(override val data: RedditCommentDto) : RedditDtoBox

@Serializable
@SerialName("t3")
data class RedditArticleBox(override val data: RedditArticleDto) : RedditDtoBox

@Serializable
sealed interface RedditDto

@Serializable
data class RedditListingDto<T: RedditDtoBox>(
    val after: RedditId? = null,
    val dist: Int? = null,
    val modhash: String? = null,
    @SerialName("geo_filter") val geoFilter: String? = null,
    val children: List<T>,
    val before: String? = null
): RedditDto

