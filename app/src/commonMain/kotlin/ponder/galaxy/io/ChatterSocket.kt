package ponder.galaxy.io

import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import ponder.galaxy.model.data.ChatterProbe

class ChatterSocket(
    private val subredditName: String,
    private val articleId: String,
) {
    private val client = globalWebsocketClient

    private val _chatterFlow = MutableSharedFlow<ChatterProbe>(replay = 1)
    val chatterFlow: SharedFlow<ChatterProbe> = _chatterFlow

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun start() {
        println("connecting chatter socket")
        client.webSocket(
            method = HttpMethod.Get,
            host = "192.168.1.100",
            port = 8080,
            path = "/chatter_probe?subreddit=$subredditName&article_id=$articleId",
        ) {
            try {
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Binary -> {
                            val probe = Cbor.decodeFromByteArray<ChatterProbe>(frame.data)
                            println("probe received: ${probe.newChatters.size}")
                            _chatterFlow.emit(probe)
                        }
                        else -> { /* ignore other frame types */ }
                    }
                }
            } finally {
                // Match ProbeSocket behavior: close and retry after delay
                println("closing chatter socket")
                close(CloseReason(CloseReason.Codes.NORMAL, "Shoving off"))
            }
        }
        println("finished start()")
    }
}