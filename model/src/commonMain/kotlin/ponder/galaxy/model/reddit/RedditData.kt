package ponder.galaxy.model.reddit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RedditData<T>(
    val kind: String,
    val data: T
)

@Serializable
data class RedditListingDto<T>(
    val after: RedditId? = null,
    val dist: Int,
    val modhash: String? = null,
    @SerialName("geo_filter") val geoFilter: String? = null,
    val children: List<T>,
    val before: String? = null
)

typealias RedditId = String

@Serializable
data class PreviewDto(
    val images: List<PreviewImageDto>,
    val enabled: Boolean
)

@Serializable
data class PreviewImageDto(
    val source: ResolutionDto,
    val resolutions: List<ResolutionDto>,
    // val variants: VariantsDto,
    val id: String
)

@Serializable
data class ResolutionDto(
    val url: String,
    val width: Int,
    val height: Int
)
