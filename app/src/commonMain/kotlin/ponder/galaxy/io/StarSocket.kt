package ponder.galaxy.io

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import ponder.galaxy.model.data.Star
import kotlin.time.Duration.Companion.seconds

class StarSocket() {

    val client = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = 15.seconds
        }
    }

    private val _starFlow = MutableStateFlow<List<Star>>(emptyList())
    val starFlow: StateFlow<List<Star>> = _starFlow

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun start() {
        client.webSocket(
            method = HttpMethod.Get,
            host = "192.168.1.100",
            port = 8080,
            path = "/starflow",
        ) {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Binary -> {
                        val list = Cbor.decodeFromByteArray<List<Star>>(frame.data)
                        _starFlow.value = list
                    }
                    else -> {} // ignore Text/Ping/Pong
                }
            }
        }
        client.close()
    }
}