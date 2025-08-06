package ponder.galaxy.server.routes

import io.ktor.server.routing.Route
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.merge
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import ponder.galaxy.model.data.GalaxyProbe
import ponder.galaxy.server.io.RedditMonitor

fun Route.serveProbeSocket(
    redditMonitor: RedditMonitor
) {

    webSocket("/probe_socket") {

        println("client connect")

        val initialProbes = redditMonitor.probeFlows.values.map { it.value }
        for (probe in initialProbes) {
            sendGalaxyProbe(probe)
        }

        merge(*redditMonitor.probeFlows.values.toTypedArray()).collect { galaxyProbe ->
            sendGalaxyProbe(galaxyProbe)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
suspend fun DefaultWebSocketServerSession.sendGalaxyProbe(galaxyProbe: GalaxyProbe) {
    val bytes = Cbor.encodeToByteArray(GalaxyProbe.serializer(), galaxyProbe)
    send(Frame.Binary(true, bytes))
}