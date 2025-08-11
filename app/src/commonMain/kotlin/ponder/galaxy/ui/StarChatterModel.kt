package ponder.galaxy.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ponder.galaxy.io.ChatterSocket
import ponder.galaxy.model.data.Chatter
import pondui.LocalValueSource
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel

class StarChatterModel(
    private val subredditName: String,
    private val articleId: String,
    private val socket: ChatterSocket = ChatterSocket(subredditName, articleId),
    private val valueSource: LocalValueSource = LocalValueSource()
): StateModel<StarChatterState>() {
    override val state = ModelState(StarChatterState())

    init {
        val rise = valueSource.readInt(RISE_FACTOR_KEY, 1)
        viewModelScope.launch(Dispatchers.IO) {

            launch {
                socket.start()
            }

            launch {
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
                    val now = Clock.System.now()
                    setState { state -> state.copy(chatters = chatters.sortedByDescending {
                        it.getRise(now, rise)
                    })}
                }
            }
        }
    }
}

data class StarChatterState(
    val chatters: List<Chatter> = emptyList()
)