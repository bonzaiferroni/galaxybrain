package ponder.galaxy.model.io

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay

class RedditClient(
    private val auth: RedditAuth,
    private val client: HttpClient = redditKtorClient
) {
    private var cachedToken: String? = null

    suspend fun me(): RedditIdentityDto = get("me").body()

    private suspend fun provideToken() = cachedToken ?: auth.authorize().also { cachedToken = it }
        ?: error("unable to provide token")

    private suspend fun get(endpoint: String): HttpResponse {
        repeat(3) {
            val token = provideToken()
            val url = "https://oauth.reddit.com/api/v1/$endpoint"
            val response = client.get(url) {
                header(HttpHeaders.Authorization, "bearer $token")
            }
            if (response.status == HttpStatusCode.OK) {
                return response
            }
            if (response.status == HttpStatusCode.Unauthorized) {
                cachedToken = null
                return@repeat
            }
            println("REDDIT API ERROR > get $endpoint: ${response.status}")
            delay(1000)
        }
        error("REDDIT API ERROR > endpoint fail: $endpoint")
    }
}

private const val BASE_URL = "https://www.reddit.com/api/v1/"

internal val redditKtorClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    defaultRequest {
        header(HttpHeaders.UserAgent, "galaxybrain/0.1")
        accept(ContentType.Application.Json)
    }
    engine {
        requestTimeout = 120_000 // Timeout in milliseconds (30 seconds here)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 120_000 // Set request timeout
        connectTimeoutMillis = 120_000 // Set connection timeout
        socketTimeoutMillis = 120_000  // Set socket timeout
    }
}