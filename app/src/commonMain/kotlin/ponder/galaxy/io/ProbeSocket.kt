package ponder.galaxy.io

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import ponder.galaxy.model.data.GalaxyProbe
import kotlin.time.Duration.Companion.seconds

class ProbeSocket() {

    val client = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = 15.seconds
        }
    }

    private val _probeFlow = MutableSharedFlow<GalaxyProbe>(replay = 1)
    val probeFlow: SharedFlow<GalaxyProbe> = _probeFlow

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun start() {
        while(true) {
            try {
                println("connecting to socket")
                client.webSocket(
                    method = HttpMethod.Get,
                    host = "192.168.1.100",
                    port = 8080,
                    path = "/probe_socket",
                ) {
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Binary -> {
                                val galaxyProbe = Cbor.decodeFromByteArray<GalaxyProbe>(frame.data)
                                _probeFlow.emit(galaxyProbe)
                            }
                            else -> {} // ignore Text/Ping/Pong
                        }
                    }
                }
            } finally {
                println("closing socket")
                client.close()
                delay(10000)
            }
        }
    }
}