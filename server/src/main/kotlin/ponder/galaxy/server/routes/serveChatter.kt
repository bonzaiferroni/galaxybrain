package ponder.galaxy.server.routes

import io.ktor.server.routing.Route
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.Instant
import kotlinx.html.Entities
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import ponder.galaxy.model.data.Chatter
import ponder.galaxy.model.data.ChatterDelta
import ponder.galaxy.model.data.ChatterProbe
import ponder.galaxy.model.reddit.ListingType
import ponder.galaxy.model.reddit.RedditClient
import ponder.galaxy.model.reddit.RedditCommentDto
import ponder.galaxy.model.reddit.flatten
import kotlin.time.Duration.Companion.minutes

fun Route.serveChatter(
    redditClient: RedditClient
) {
    webSocket("/chatter_probe") {
        val subredditName = call.request.queryParameters["subreddit"] ?: error("subreddit not found")
        val articleId = call.request.queryParameters["article_id"] ?: error("articleId not found")

        println("chatter probe: $subredditName $articleId")

        val sentCommentIds = mutableSetOf<String>()
        while (isActive) {
            val comments = redditClient.getComments(subredditName, articleId, ListingType.Top)?.flatten()
            if (comments == null) {
                delay(1.minutes)
                continue
            }
            println("found ${comments.size} comments")
            val visibilitySum = comments.sumOf { it.deriveVisibility().toDouble() }.toFloat()
            val averageVisibility = visibilitySum / comments.size
            val chatters = mutableListOf<Chatter>()
            val deltas = mutableListOf<ChatterDelta>()
            for (comment in comments) {
                val visibility = comment.deriveVisibility()
                val visibilityRatio = visibility / averageVisibility
                val createdAt = Instant.fromEpochSeconds(comment.createdUtc.toLong())
                if (sentCommentIds.contains(comment.id)) {
                    deltas.add(ChatterDelta(
                        identifier = comment.id,
                        visibility = visibility,
                        visibilityRatio = visibilityRatio
                    ))
                } else {
                    val body = comment.body
                    sentCommentIds.add(comment.id)
                    chatters.add(Chatter(
                        identifier = comment.id,
                        body = body,
                        author = comment.author,
                        permalink = "https://www.reddit.com${comment.permalink}",
                        depth = comment.depth,
                        visibility = visibility,
                        visibilityRatio = visibilityRatio,
                        createdAt = createdAt
                    ))
                }
            }
            sendChatterProbe(ChatterProbe(
                newChatters = chatters,
                deltas = deltas
            ))
            delay(1000 * 60)
        }
    }
}

fun RedditCommentDto.deriveVisibility() = (replies.size * 2 + score).toFloat()

@OptIn(ExperimentalSerializationApi::class)
suspend fun DefaultWebSocketServerSession.sendChatterProbe(chatterProbe: ChatterProbe) {
    val bytes = Cbor.encodeToByteArray(ChatterProbe.serializer(), chatterProbe)
    send(Frame.Binary(true, bytes))
}