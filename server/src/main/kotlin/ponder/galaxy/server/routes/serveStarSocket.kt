package ponder.galaxy.server.routes

import io.ktor.server.routing.Route
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.cbor.Cbor
import ponder.galaxy.model.data.GalaxyProbe
import ponder.galaxy.model.data.StarLog
import ponder.galaxy.server.io.RedditMonitor

@OptIn(ExperimentalSerializationApi::class)
fun Route.serveStarSocket(
    redditMonitor: RedditMonitor
) {
    val syncClients = mutableSetOf<DefaultWebSocketServerSession>()
    val syncLock = Mutex()

    webSocket("/starsocket") {
        syncLock.withLock { syncClients += this }

        println("client connect")

        try {
            redditMonitor.galaxyProbeFlow.collect { galaxyProbe ->
                val bytes = Cbor.encodeToByteArray(GalaxyProbe.serializer(), galaxyProbe)
                syncLock.withLock { syncClients.forEach { it.send(Frame.Binary(true, bytes)) } }
            }
        } finally {
            // remove on disconnect
            syncLock.withLock { syncClients -= this }
        }
    }
}