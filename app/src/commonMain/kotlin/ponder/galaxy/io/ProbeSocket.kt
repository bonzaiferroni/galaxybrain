package ponder.galaxy.io

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import ponder.galaxy.model.data.GalaxyProbe
import kotlin.time.Duration.Companion.seconds

class ProbeSocket() {

    val client = globalWebsocketClient

    private val _probeFlow = MutableSharedFlow<GalaxyProbe>(replay = 1)
    val probeFlow: SharedFlow<GalaxyProbe> = _probeFlow

    @OptIn(ExperimentalSerializationApi::class)
    fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            while(isActive) {
                println("connecting to galaxy socket")
                try {
                    client.webSocket(
                        method = HttpMethod.Get,
                        host = "192.168.1.100",
                        port = 8080,
                        path = "/probe_socket",
                    ) {
                        try {
                            for (frame in incoming) {
                                when (frame) {
                                    is Frame.Binary -> {
                                        val galaxyProbe = Cbor.decodeFromByteArray<GalaxyProbe>(frame.data)
                                        _probeFlow.emit(galaxyProbe)
                                    }
                                    else -> {} // ignore Text/Ping/Pong
                                }
                            }
                        } finally {
                            println("closing galaxy socket")
                            close(CloseReason(CloseReason.Codes.NORMAL, "Shoving off"))
                        }
                    }
                } catch (e: Exception) {
                    println("error connecting to galaxy socket: ${e.message}")
                }
                if (isActive) {
                    println("reconnecting galaxy socket after delay")
                    delay(10000)
                }
            }
        }
    }
}

val globalWebsocketClient = HttpClient(CIO) {
    install(WebSockets) {
        pingInterval = 15.seconds
    }
}