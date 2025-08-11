package ponder.galaxy.model.reddit

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable
data class RedditCommentDto(
    val id: String,
    @SerialName("parent_id") val parentId: String,
    @SerialName("link_id") val linkId: String? = null,
    val author: String,
    val body: String,
    val permalink: String,
    @SerialName("body_html") val bodyHtml: String? = null,
    // JSON shows seconds as a float; keep Double to match the payload
    @SerialName("created_utc") val createdUtc: Double,
    val score: Int,
    val depth: Int? = null,
    @Serializable(with = CommentRepliesSerializer::class)
    val replies: List<RedditCommentDto> = emptyList()
): RedditDto

object CommentRepliesSerializer : KSerializer<List<RedditCommentDto>> {
    private val delegate = JsonArray.serializer()

    override val descriptor = delegate.descriptor

    override fun deserialize(decoder: Decoder): List<RedditCommentDto> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("CommentRepliesSerializer requires Json")
        val elem = jsonDecoder.decodeJsonElement()

        // replies can be "" or a Listing object; normalize to a flat list of child comments
        return when (elem) {
            is JsonPrimitive -> if (elem.isString && elem.content.isEmpty()) {
                emptyList()
            } else emptyList()

            is JsonObject -> {
                val children = elem["data"]
                    ?.jsonObject
                    ?.get("children")
                    ?.jsonArray
                    ?: return emptyList()

                children.mapNotNull { child ->
                    val data = child.jsonObject["data"] ?: return@mapNotNull null
                    jsonDecoder.json.decodeFromJsonElement(RedditCommentDto.serializer(), data)
                }
            }

            else -> emptyList()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: List<RedditCommentDto>) {
        // Not needed for our use; write null to keep payload minimal
        encoder.encodeNull()
    }
}

fun List<RedditCommentDto>.flatten(): List<RedditCommentDto> {
    val out = mutableListOf<RedditCommentDto>()
    val stack = ArrayDeque<RedditCommentDto>()
    for (c in this.asReversed()) stack.addLast(c)
    while (stack.isNotEmpty()) {
        val c = stack.removeLast()
        out += c
        for (child in c.replies.asReversed()) stack.addLast(child)
    }
    return out
}