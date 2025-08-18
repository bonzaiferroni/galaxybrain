package ponder.galaxy.model.data

import kabinet.db.TableId
import kabinet.utils.randomUuidString
import kabinet.web.Url
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
data class StarLink(
    val starLinkId: StarLinkId,
    val fromStarId: StarId?,
    val toStarId: StarId?,
    val snippetId: SnippetId?,
    val commentId: CommentId?,
    val url: Url,
    val text: String?,
    val startIndex: Int?,
    val createdAt: Instant,
)

@JvmInline @Serializable
value class StarLinkId(override val value: String): TableId<String> {
    companion object {
        fun random() = StarLinkId(randomUuidString())
    }
}