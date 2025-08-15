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
    suspend fun gatherComments(
        parentId: CommentId? = null,
        commentDtos: List<RedditCommentDto>,
        comments: MutableList<Comment>,
        starId: StarId,
        averageVisibility: Float,
        now: Instant,
    ) {
        val dbComments = dao.readByIdentifiers(commentDtos.map { it.id }).map { dbComment ->
            val commentDto = commentDtos.first { it.id == dbComment.identifier }
            val visibility = commentDto.deriveVisibility()
            val visibilityRatio = visibility / averageVisibility
            dbComment.copy(
                text = commentDto.body.takeIf { it.isNotEmpty() } ?: dbComment.text,
                voteCount = commentDto.score,
                replyCount = commentDto.replies.size,
                visibility = visibility,
                visibilityRatio = visibilityRatio,
                updatedAt = now
            )
        }
        dao.update(dbComments)
        comments.addAll(dbComments)

        val newComments = commentDtos.mapNotNull { commentDto ->
            if (dbComments.any { it.identifier == commentDto.id }) return@mapNotNull null
            val visibility = commentDto.deriveVisibility()
            val visibilityRatio = visibility / averageVisibility
            Comment(
                commentId = CommentId(generateUuidString()),
                parentId = parentId,
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
        }
        dao.insert(newComments)
        comments.addAll(newComments)

        for (commentDto in commentDtos) {
            if (commentDto.replies.isEmpty()) continue
            val comment = comments.first { it.identifier == commentDto.id }
            gatherComments(
                parentId = comment.commentId,
                commentDtos = commentDto.replies,
                comments = comments,
                starId = starId,
                averageVisibility = averageVisibility,
                now = now
            )
        }
    }
}