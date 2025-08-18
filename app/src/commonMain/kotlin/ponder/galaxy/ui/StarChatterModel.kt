package ponder.galaxy.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ponder.galaxy.io.CommentSocket
import ponder.galaxy.io.IdeaApiClient
import ponder.galaxy.model.data.Comment
import ponder.galaxy.model.data.CommentId
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.Idea
import ponder.galaxy.model.data.Snippet
import ponder.galaxy.model.data.StarId
import pondui.LocalValueSource
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel

class StarChatterModel(
    private val galaxyId: GalaxyId,
    private val starId: StarId,
    private val socket: CommentSocket = CommentSocket(galaxyId, starId),
    private val valueSource: LocalValueSource = LocalValueSource(),
    private val ideaApiClient: IdeaApiClient = IdeaApiClient(),
) : StateModel<StarChatterState>() {
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
                    setState { state ->
                        state.copy(
                            comments = chatters.sortedByDescending {
                                it.getRise(now, rise)
                            },
                            snippets = state.snippets + commentProbe.snippets
                        )
                    }
                }
            }
        }
    }

    fun startNextIdea() {
        val currentIdea = stateNow.currentIdea
        val freshComments =
            stateNow.comments.filter { comment -> stateNow.ideas.none { it.commentId == comment.commentId } }
        val nextComment = currentIdea?.let { freshComments.firstOrNull { it.parentId == currentIdea.commentId } }
            ?: freshComments.firstOrNull()?.let { getRootComment(it, freshComments) }
            ?: return
        viewModelScope.launch {
            val idea = ideaApiClient.readCommentIdea(nextComment.commentId, true) ?: return@launch
            setState { it.copy(currentIdea = idea, ideas = it.ideas + idea) }
        }
    }

    fun toggleIsPlaying(value: Boolean = !stateNow.isPlaying) {
        setState { it.copy(isPlaying = value) }
        if (value) startNextIdea()
    }
}

private fun getRootComment(comment: Comment, comments: List<Comment>): Comment {
    val parent = comment.parentId?.let { parentId -> comments.firstOrNull { it.commentId == parentId } }
    return parent?.let { getRootComment(it, comments) } ?: comment
}

data class StarChatterState(
    val comments: List<Comment> = emptyList(),
    val ideas: List<Idea> = emptyList(),
    val snippets: Map<CommentId, List<Snippet>> = emptyMap(),
    val currentIdea: Idea? = null,
    val isPlaying: Boolean = false,
)