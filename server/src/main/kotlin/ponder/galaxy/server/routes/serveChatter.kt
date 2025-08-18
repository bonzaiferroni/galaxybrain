@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.routes

import io.ktor.server.routing.Route
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kabinet.utils.toUuid
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import ponder.galaxy.model.data.CommentDelta
import ponder.galaxy.model.data.CommentProbe
import ponder.galaxy.model.data.Comment
import ponder.galaxy.model.data.CommentId
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.Snippet
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.reddit.ListingType
import ponder.galaxy.model.reddit.RedditClient
import ponder.galaxy.model.reddit.RedditCommentDto
import ponder.galaxy.model.reddit.flatten
import ponder.galaxy.server.db.services.CommentService
import ponder.galaxy.server.db.services.GalaxyTableDao
import ponder.galaxy.server.db.services.StarTableDao
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.ExperimentalUuidApi

fun Route.serveChatter(
    redditClient: RedditClient,
    galaxyDao: GalaxyTableDao = GalaxyTableDao(),
    starDao: StarTableDao = StarTableDao(),
    commentService: CommentService = CommentService(),
) {
    webSocket("/comment_probe") {
        val galaxyId = call.request.queryParameters["galaxy_id"]?.let { GalaxyId(it) } ?: error("subreddit not found")
        val starId = call.request.queryParameters["star_id"]?.let { StarId(it) } ?: error("articleId not found")

        val galaxy = galaxyDao.readById(galaxyId)
        val star = starDao.readByIdOrNull(starId) ?: error("star not found")

        val subredditName = galaxy.name
        val articleId = star.identifier ?: error("Article id not found")

        println("chatter probe: $subredditName $articleId")

        val sentCommentIds = mutableSetOf<CommentId>()
        while (isActive) {
            val commentDtos = redditClient.getComments(subredditName, articleId, ListingType.Top)
            if (commentDtos == null) {
                delay(1.minutes)
                continue
            }

            val commentsFlat = commentDtos.flatten()
            val visibilitySum = commentsFlat.sumOf { it.deriveVisibility().toDouble() }.toFloat()
            val averageVisibility = visibilitySum / commentDtos.size

            println("found ${commentDtos.size} comments")

            val comments = mutableListOf<Comment>()
            val now = Clock.System.now()
            val snippetMap = mutableMapOf<CommentId, List<Snippet>>()
            commentService.gatherComments(
                parentId = null,
                commentDtos = commentDtos,
                comments = comments,
                snippetMap = snippetMap,
                starId = starId,
                averageVisibility = averageVisibility,
                now = now
            )
            val newComments = mutableListOf<Comment>()
            val deltas = mutableListOf<CommentDelta>()
            for (comment in comments) {
                if (sentCommentIds.contains(comment.commentId)) {
                    deltas.add(CommentDelta(
                        commentId = comment.commentId,
                        visibility = comment.visibility,
                        visibilityRatio = comment.visibilityRatio,
                        voteCount = comment.voteCount,
                        replyCount = comment.replyCount,
                    ))
                } else {
                    sentCommentIds.add(comment.commentId)
                    newComments.add(comment)
                }
            }
            sendChatterProbe(CommentProbe(
                newComments = newComments,
                deltas = deltas,
                snippets = snippetMap,
            ))
            delay(1000 * 60)
        }
    }
}

fun RedditCommentDto.deriveVisibility() = (replies.size * 2 + score).toFloat()

@OptIn(ExperimentalSerializationApi::class)
suspend fun DefaultWebSocketServerSession.sendChatterProbe(commentProbe: CommentProbe) {
    val bytes = Cbor.encodeToByteArray(CommentProbe.serializer(), commentProbe)
    send(Frame.Binary(true, bytes))
}