package ponder.galaxy.model.reddit

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.path
import kotlinx.coroutines.delay

class RedditClient(
    private val auth: RedditAuth,
    private val client: HttpClient = redditKtorClient
) {
    private var cachedToken: String? = null

    private suspend fun provideToken() = cachedToken ?: auth.authorize().also { cachedToken = it }
    ?: error("unable to provide token")

    private suspend fun request(request: HttpRequestBuilder): HttpResponse? {
        repeat(3) {
            try {
                val response = client.request(request)
                if (response.status == HttpStatusCode.OK) {
                    return response
                }
                if (response.status == HttpStatusCode.Unauthorized) {
                    cachedToken = null
                    return@repeat
                }
                println("REDDIT API ERROR > get ${request.url}: ${response.status}")
            } catch (e: Exception) {
                println("RedditClient: $e")
            }
            delay(1000)
        }
        error("REDDIT API ERROR > endpoint fail: ${request.url}")
    }

    private suspend fun buildRequest(endpoint: String, method: HttpMethod = HttpMethod.Get) = HttpRequestBuilder().apply {
        this.method = method
        url {
            host = "oauth.reddit.com"
            path(endpoint)
        }
        header(HttpHeaders.Authorization, "bearer ${provideToken()}")
        parameter("raw_json", 1)
    }

    suspend fun me(): RedditIdentityDto? = request(buildRequest("api/v1/me"))?.body()

//    suspend fun best(): List<RedditLinkDto> {
//        val response = request(buildRequest("best"))
//        val data: RedditData<RedditListingDto<RedditData<RedditLinkDto>>> = response.body()
//        return data.data.children.map { it.data }
//    }

    suspend fun getArticles(subreddit: String, listingType: ListingType): List<RedditArticleDto>? {
        val response = request(buildRequest("r/$subreddit/${listingType.urlValue}").apply {
            parameter("count", 100)
        }) ?: return null
        val box: RedditListingBox<RedditArticleBox> = response.body()
        return box.data.children.map { it.data }
    }

    suspend fun getComments(
        subreddit: String,
        articleId: String,
        listingType: ListingType = ListingType.Top
    ): List<RedditCommentDto>? {
        val response = request(buildRequest("r/$subreddit/comments/$articleId").apply {
            parameter("sort", listingType.urlValue)
            parameter("limit", 1000)
            parameter("depth", 20)
            parameter("showmore", false)
            // parameter("context", 8)

            // parameter("count", 100)
        }) ?: return null
        val boxes: List<RedditListingBox<RedditDtoBox>> = response.body()
        return boxes[1].data.children.map { it.data as RedditCommentDto }
    }
}

const val REDDIT_URL_BASE = "https://www.reddit.com"

enum class ListingType(val urlValue: String) {
    New("new"),
    Rising("rising"),
    Hot("hot"),
    Best("best"),
    Top("top"),
    Controversial("controversial")
}