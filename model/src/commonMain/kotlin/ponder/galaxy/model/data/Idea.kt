package ponder.galaxy.model.data

import kabinet.db.TableId
import kabinet.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Idea(
    val ideaId: IdeaId,
    val starId: StarId?,
    val commentId: CommentId?,
    val description: String,
    val audioUrl: String?,
    val text: String?,
    val imageUrl: String?,
    val thumbUrl: String?,
    val createdAt: Instant,
)

@JvmInline
@Serializable
value class IdeaId(override val value: String): TableId<String> {
    companion object {
        fun random() = IdeaId(randomUuidString())
    }
}