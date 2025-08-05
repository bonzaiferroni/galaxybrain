package ponder.galaxy.io

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarLog
import kotlin.time.Duration.Companion.seconds

class StarSocket() {

    val client = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = 15.seconds
        }
    }

    private val _starLogFlow = MutableStateFlow<List<StarLog>>(emptyList())
    val starLogFlow: StateFlow<List<StarLog>> = _starLogFlow

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun start() {
        client.webSocket(
            method = HttpMethod.Get,
            host = "192.168.1.100",
            port = 8080,
            path = "/starsocket",
        ) {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Binary -> {
                        val list = Cbor.decodeFromByteArray<List<StarLog>>(frame.data)
                        _starLogFlow.value = list
                    }
                    else -> {} // ignore Text/Ping/Pong
                }
            }
        }
        client.close()
    }
}