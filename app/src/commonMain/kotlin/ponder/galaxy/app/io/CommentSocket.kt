package ponder.galaxy.app.io

import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import ponder.galaxy.model.data.CommentProbe
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.StarId

class CommentSocket(
    private val galaxyId: GalaxyId,
    private val starId: StarId,
) {
    private val client = globalWebsocketClient

    private val _commentFlow = MutableSharedFlow<CommentProbe>(replay = 1)
    val commentFlow: SharedFlow<CommentProbe> = _commentFlow

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun start() {
        println("connecting comment socket")
        client.webSocket(
            method = HttpMethod.Get,
            host = "192.168.1.100",
            port = 8080,
            path = "/comment_probe?galaxy_id=${galaxyId.value}&star_id=${starId.value}",
        ) {
            try {
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Binary -> {
                            val probe = Cbor.decodeFromByteArray<CommentProbe>(frame.data)
                            println("probe received: ${probe.newComments.size}")
                            _commentFlow.emit(probe)
                        }
                        else -> { /* ignore other frame types */ }
                    }
                }
            } finally {
                // Match ProbeSocket behavior: close and retry after delay
                println("closing comment socket")
                close(CloseReason(CloseReason.Codes.NORMAL, "Shoving off"))
            }
        }
        println("finished start()")
    }
}