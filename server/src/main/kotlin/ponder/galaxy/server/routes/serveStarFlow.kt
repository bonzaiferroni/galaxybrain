package ponder.galaxy.server.routes

import io.ktor.server.application.Application
import io.ktor.server.routing.Route
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import ponder.galaxy.model.data.Star
import ponder.galaxy.server.io.RedditMonitor

@OptIn(ExperimentalSerializationApi::class)
fun Route.serveStarFlow(
    redditMonitor: RedditMonitor
) {
    val syncClients = mutableSetOf<DefaultWebSocketServerSession>()
    val syncLock = Mutex()

    webSocket("/starflow") {
        syncLock.withLock { syncClients += this }

        println("client connect")

        try {
            redditMonitor.starFlow.collect { stars ->
                val bytes = Cbor.encodeToByteArray(ListSerializer(Star.serializer()), stars)
                syncLock.withLock { syncClients.forEach { it.send(Frame.Binary(true, bytes)) } }
            }
        } finally {
            // remove on disconnect
            syncLock.withLock { syncClients -= this }
        }
    }
}