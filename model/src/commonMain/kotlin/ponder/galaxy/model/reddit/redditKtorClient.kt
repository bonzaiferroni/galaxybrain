package ponder.galaxy.model.reddit

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


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