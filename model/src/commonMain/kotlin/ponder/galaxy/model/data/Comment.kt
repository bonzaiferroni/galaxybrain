package ponder.galaxy.model.data

import kabinet.db.TableId
import kabinet.model.SpeechVoice
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Comment(
    val commentId: CommentId,
    val parentId: CommentId?,
    val starId: StarId,
    val identifier: String,
    val author: String,
    val depth: Int?,
    val voteCount: Int,
    val replyCount: Int,
    val visibility: Float,
    val visibilityRatio: Float,
    val permalink: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val accessedAt: Instant,
) {
    fun getRise(now: Instant, riseFactor: Int) = calculateRise(now - createdAt, visibilityRatio, riseFactor)
    val intrinsicIndex get() = author.length
    fun getVoice() = SpeechVoice.getByIntrinsicIndex(intrinsicIndex)
}

@JvmInline
@Serializable
value class CommentId(override val value: String): TableId<String>

@Serializable
data class CommentDelta(
    val commentId: CommentId,
    val visibility: Float,
    val visibilityRatio: Float,
    val voteCount: Int,
    val replyCount: Int,
)

@Serializable
data class CommentProbe(
    val newComments: List<Comment>,
    val deltas: List<CommentDelta>,
    val snippets: Map<CommentId, List<Snippet>>
)