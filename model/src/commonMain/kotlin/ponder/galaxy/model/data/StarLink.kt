@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.model.data

import kabinet.db.TableId
import kabinet.web.Url
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
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
value class StarLinkId(override val value: Uuid): TableId<Uuid> {
    companion object {
        fun random() = StarLinkId(Uuid.random())
    }
}