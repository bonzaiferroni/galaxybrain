package ponder.galaxy.io

import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
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
import ponder.galaxy.model.data.ChatterProbe

class ChatterSocket(
    private val subredditName: String,
    private val articleId: String,
) {

    private val client = globalWebsocketClient

    private val _chatterFlow = MutableSharedFlow<ChatterProbe>(replay = 1)
    val chatterFlow: SharedFlow<ChatterProbe> = _chatterFlow

    @OptIn(ExperimentalSerializationApi::class)
    fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            println("connecting chatter socket")
            while (isActive) {
                try {
                    client.webSocket(
                        method = HttpMethod.Get,
                        host = "192.168.1.100",
                        port = 8080,
                        path = "/chatter_probe?subreddit=$subredditName&article_id=$articleId",
                    ) {
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
                    }
                } finally {
                    // Match ProbeSocket behavior: close and retry after delay
                    client.close()
                    delay(10_000)
                }
            }
        }
    }
}