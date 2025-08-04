package ponder.galaxy.model.reddit

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.http.HttpHeaders
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay

class RedditClient(
    private val auth: RedditAuth,
    private val client: HttpClient = redditKtorClient
) {
    private var cachedToken: String? = null

    private suspend fun provideToken() = cachedToken ?: auth.authorize().also { cachedToken = it }
    ?: error("unable to provide token")

    private suspend fun get(endpoint: String): HttpResponse {
        repeat(3) {
            val token = provideToken()
            val url = "https://oauth.reddit.com/$endpoint"
            val response = client.get(url) {
                header(HttpHeaders.Authorization, "bearer $token")
                parameter("raw_json", 1)
            }
            if (response.status == HttpStatusCode.OK) {
                return response
            }
            if (response.status == HttpStatusCode.Unauthorized) {
                cachedToken = null
                return@repeat
            }
            error("REDDIT API ERROR > get $endpoint: ${response.status}")
        }
        error("REDDIT API ERROR > endpoint fail: $endpoint")
    }

    suspend fun me(): RedditIdentityDto = get("api/v1/me").body()

    suspend fun best(): List<RedditLinkDto> {
        val response = get("best")
        val data: RedditData<RedditListingDto<RedditData<RedditLinkDto>>> = response.body()
        return data.data.children.map { it.data }
    }

    suspend fun getListing(subreddit: String, listingType: ListingType): List<RedditLinkDto> {
        val response = get("r/$subreddit/${listingType.urlFragment}")
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