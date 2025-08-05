package ponder.galaxy.model.reddit

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
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

    private suspend fun request(request: HttpRequestBuilder): HttpResponse {
        repeat(3) {
            val response = client.request(request)
            if (response.status == HttpStatusCode.OK) {
                return response
            }
            if (response.status == HttpStatusCode.Unauthorized) {
                cachedToken = null
                return@repeat
            }
            error("REDDIT API ERROR > get ${request.url}: ${response.status}")
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

    suspend fun me(): RedditIdentityDto = request(buildRequest("api/v1/me")).body()

    suspend fun best(): List<RedditLinkDto> {
        val response = request(buildRequest("best"))
        val data: RedditData<RedditListingDto<RedditData<RedditLinkDto>>> = response.body()
        return data.data.children.map { it.data }
    }

    suspend fun getListing(subreddit: String, listingType: ListingType): List<RedditLinkDto> {
        val response = request(buildRequest("r/$subreddit/${listingType.urlFragment}").apply {
            parameter("count", 100)
        })
        val data: RedditData<RedditListingDto<RedditData<RedditLinkDto>>> = response.body()
        return data.data.children.map { it.data }
    }
}

const val REDDIT_URL_BASE = "https://www.reddit.com"

enum class ListingType(val urlFragment: String) {
    New("new"),
    Rising("rising"),
    Hot("hot"),
    Best("best"),
    Top("top"),
    Controversial("controversial")
}