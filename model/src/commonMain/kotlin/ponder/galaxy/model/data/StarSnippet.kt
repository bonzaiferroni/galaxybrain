package ponder.galaxy.model.data

import kabinet.db.TableId
import kabinet.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

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
value class StarSnippetId(override val value: String): TableId<String> {
    companion object {
        fun random() = StarSnippetId(randomUuidString())
    }
}