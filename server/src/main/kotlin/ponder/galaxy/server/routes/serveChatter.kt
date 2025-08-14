package ponder.galaxy.server.routes

import io.ktor.server.routing.Route
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
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
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.reddit.ListingType
import ponder.galaxy.model.reddit.RedditClient
import ponder.galaxy.model.reddit.RedditCommentDto
import ponder.galaxy.model.reddit.flatten
import ponder.galaxy.server.db.services.CommentService
import ponder.galaxy.server.db.services.GalaxyTableDao
import ponder.galaxy.server.db.services.StarTableDao
import kotlin.time.Duration.Companion.minutes

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
        val articleId = star.identifier

        println("chatter probe: $subredditName $articleId")

        val sentCommentIds = mutableSetOf<CommentId>()
        while (isActive) {
            val commentDtos = redditClient.getComments(subredditName, articleId, ListingType.Top)?.flatten()
            if (commentDtos == null) {
                delay(1.minutes)
                continue
            }

            val visibilitySum = commentDtos.sumOf { it.deriveVisibility().toDouble() }.toFloat()
            val averageVisibility = visibilitySum / commentDtos.size

            println("found ${commentDtos.size} comments")

            val comments = mutableListOf<Comment>()
            val deltas = mutableListOf<CommentDelta>()
            val now = Clock.System.now()
            for (commentDto in commentDtos) {
                val comment = commentService.insertOrUpdateComment(
                    commentDto = commentDto,
                    comments = commentDtos,
                    starId = starId,
                    visibilitySum = visibilitySum,
                    averageVisibility = averageVisibility,
                    now = now
                )
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
                    comments.add(comment)
                }
            }
            sendChatterProbe(CommentProbe(
                newComments = comments,
                deltas = deltas
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