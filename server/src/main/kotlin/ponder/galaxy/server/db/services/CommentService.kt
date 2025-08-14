package ponder.galaxy.server.db.services

import kabinet.utils.generateUuidString
import kabinet.utils.fromEpochSecondsDouble
import kotlinx.datetime.Instant
import ponder.galaxy.model.data.Comment
import ponder.galaxy.model.data.CommentId
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.reddit.RedditCommentDto
import ponder.galaxy.server.routes.deriveVisibility

class CommentService(
    val dao: CommentTableDao = CommentTableDao()
) {
    suspend fun insertOrUpdateComment(
        commentDto: RedditCommentDto,
        comments: List<RedditCommentDto>,
        starId: StarId,
        visibilitySum: Float,
        averageVisibility: Float,
        now: Instant,
    ): Comment {
        val visibility = commentDto.deriveVisibility()
        val visibilityRatio = visibility / averageVisibility

        var comment = dao.readByIdentifier(commentDto.id)
        if (comment != null) {
            if (comment.updatedAt != now) {
                comment = comment.copy(
                    text = commentDto.body.takeIf { it.isNotEmpty() } ?: comment.text,
                    voteCount = commentDto.score,
                    replyCount = commentDto.replies.size,
                    visibility = visibility,
                    visibilityRatio = visibilityRatio,
                    updatedAt = now
                )
                dao.update(comment)
            }
            return comment
        }

        val parent = commentDto.parentId
            ?.let { parentId -> comments.firstOrNull { it.id == parentId } }
            ?.let { parentDto -> insertOrUpdateComment(
                commentDto = parentDto,
                comments = comments,
                starId = starId,
                visibilitySum = visibilitySum,
                averageVisibility = averageVisibility,
                now = now
            ) }

        comment = Comment(
            commentId = CommentId(generateUuidString()),
            parentId = parent?.commentId,
            starId = starId,
            identifier = commentDto.id,
            author = commentDto.author,
            text = commentDto.body,
            depth = commentDto.depth,
            voteCount = commentDto.score,
            replyCount = commentDto.replies.size,
            visibility = visibility,
            visibilityRatio = visibilityRatio,
            permalink = "https://www.reddit.com${commentDto.permalink}",
            createdAt = Instant.fromEpochSecondsDouble(commentDto.createdUtc),
            updatedAt = now,
            accessedAt = now
        )
        dao.insert(comment)
        return comment
    }
}