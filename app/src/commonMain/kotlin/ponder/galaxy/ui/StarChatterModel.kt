package ponder.galaxy.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ponder.galaxy.io.CommentSocket
import ponder.galaxy.model.data.Chatter
import ponder.galaxy.model.data.Comment
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.StarId
import pondui.LocalValueSource
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel

class StarChatterModel(
    private val galaxyId: GalaxyId,
    private val starId: StarId,
    private val socket: CommentSocket = CommentSocket(galaxyId, starId),
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
                socket.commentFlow.collect { commentProbe ->
                    val chatters = stateNow.comments.map { comment ->
                        val delta = commentProbe.deltas.firstOrNull { it.commentId == comment.commentId }
                        if (delta != null) {
                            comment.copy(
                                visibility = comment.visibility,
                                visibilityRatio = comment.visibilityRatio,
                                voteCount = comment.voteCount,
                                replyCount = comment.replyCount,
                            )
                        } else comment
                    } + commentProbe.newComments
                    val now = Clock.System.now()
                    setState { state -> state.copy(comments = chatters.sortedByDescending {
                        it.getRise(now, rise)
                    })}
                }
            }
        }
    }
}

data class StarChatterState(
    val comments: List<Comment> = emptyList()
)