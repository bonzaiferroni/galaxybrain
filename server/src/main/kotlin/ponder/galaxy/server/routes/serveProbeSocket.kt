package ponder.galaxy.server.routes

import io.ktor.server.routing.Route
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kabinet.console.globalConsole
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.io.IOException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import ponder.galaxy.model.data.GalaxyProbe
import ponder.galaxy.server.io.RedditMonitor

private val console = globalConsole.getHandle("serveProbeSocket")

fun Route.serveProbeSocket(
    redditMonitor: RedditMonitor
) {

    webSocket("/probe_socket") {

        console.log("client connect")

        try {
            // snapshot + drop nulls
            val flows = redditMonitor.getFlows()

            val initialProbes = flows.map { it.value }
            for (probe in initialProbes) {
                sendGalaxyProbe(probe)
            }

            merge(*flows.toTypedArray()).collect { galaxyProbe ->
                sendGalaxyProbe(galaxyProbe)
            }
        } catch (ioe: IOException) {
            // handle ping timeout
            console.log("Ping timeout: ${ioe.message}")
            close(CloseReason(CloseReason.Codes.GOING_AWAY, "Ping timeout"))
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
suspend fun DefaultWebSocketServerSession.sendGalaxyProbe(galaxyProbe: GalaxyProbe) {
    val bytes = Cbor.encodeToByteArray(GalaxyProbe.serializer(), galaxyProbe)
    send(Frame.Binary(true, bytes))
}