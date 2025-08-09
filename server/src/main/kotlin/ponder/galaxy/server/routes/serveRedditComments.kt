package ponder.galaxy.server.routes

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import ponder.galaxy.model.reddit.ListingType
import ponder.galaxy.model.reddit.RedditClient

fun Route.serveRedditComments(
    redditClient: RedditClient
) {
    webSocket("/reddit_comments") {
        val subredditName = call.request.queryParameters["subreddit"] ?: error("subreddit not found")
        val articleId = call.request.queryParameters["article_id"] ?: error("articleId not found")
        redditClient.getComments(subredditName, articleId, ListingType.Top)
    }
}