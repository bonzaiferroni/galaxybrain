package ponder.galaxy.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ponder.galaxy.io.ChatterSocket
import ponder.galaxy.model.data.Chatter
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel

class StarChatterModel(
    private val subredditName: String,
    private val articleId: String,
    private val socket: ChatterSocket = ChatterSocket(subredditName, articleId)
): StateModel<StarChatterState>() {
    override val state = ModelState(StarChatterState())

    init {
        socket.start()

        viewModelScope.launch(Dispatchers.IO) {
            socket.chatterFlow.collect { chatterProbe ->
                val chatters = stateNow.chatters.map { chatter ->
                    val delta = chatterProbe.deltas.firstOrNull { it.identifier == chatter.identifier }
                    if (delta != null) {
                        chatter.copy(
                            visibility = chatter.visibility,
                            visibilityRatio = chatter.visibilityRatio
                        )
                    } else chatter
                } + chatterProbe.newChatters
                println("chatters for state: ${chatters.size}")
                setState { state -> state.copy(chatters = chatters.sortedByDescending { it.visibility })}
            }
        }
    }
}

data class StarChatterState(
    val chatters: List<Chatter> = emptyList()
)