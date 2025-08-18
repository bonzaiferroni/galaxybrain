@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.model.data

import kabinet.db.TableId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class StarSnippet(
    val starSnippetId: StarSnippetId,
    val snippetId: SnippetId,
    val starId: StarId,
    val commentId: CommentId?,
    val order: Int,
    val createdAt: Instant,
)

@JvmInline @Serializable
value class StarSnippetId(override val value: Uuid): TableId<Uuid> {
    companion object {
        fun random() = StarSnippetId(Uuid.random())
    }
}