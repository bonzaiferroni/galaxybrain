package ponder.galaxy.model.reddit

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import io.ktor.util.encodeBase64
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class RedditAuth(
    private val username: String,
    private val password: String,
    private val appId: String,
    private val appSecret: String,
    private val client: HttpClient = redditKtorClient
) {
    suspend fun authorize(): String? {
        val userPass = "${appId}:${appSecret}"
        val encoded  = userPass.encodeBase64()
        val bodyStr = "grant_type=password&username=$username&password=$password"
        val response = client.post("https://www.reddit.com/api/v1/access_token") {
            header(HttpHeaders.Authorization, "Basic $encoded")
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(TextContent(bodyStr, ContentType.Application.FormUrlEncoded))
        }
        val tokenResponse: RedditTokenResponse = response.body()
        return tokenResponse.accessToken
    }
}

@Serializable
data class RedditTokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("expires_in")
    val expiresIn: Long,
    val scope: String
)

const val REDDIT_USERNAME_KEY = "REDDIT_USERNAME"
const val REDDIT_PASSWORD_KEY = "REDDIT_PASSWORD"
const val REDDIT_APP_ID_KEY = "REDDIT_APP_ID"
const val REDDIT_APP_SECRET_KEY = "REDDIT_APP_SECRET"