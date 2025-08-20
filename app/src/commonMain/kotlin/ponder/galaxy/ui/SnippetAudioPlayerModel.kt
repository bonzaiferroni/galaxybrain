package ponder.galaxy.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ponder.galaxy.io.SnippetApiClient
import ponder.galaxy.model.data.SnippetId
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel

class SnippetAudioPlayerModel(
    private val snippetClient: SnippetApiClient = SnippetApiClient()
): StateModel<SnippetAudioPlayerState>() {
    override val state = ModelState(SnippetAudioPlayerState())

    private var buffer: Array<String?> = emptyArray()

    fun toggleIsPlaying(isPlaying: Boolean = !stateNow.isPlaying) {
        if (isPlaying) {
            setState { it.copy(isPlaying = true, position = 0) }
            bufferNext(0)
        } else {
            setState { it.copy(isPlaying = false)}
        }
    }

    fun load(snippetIds: List<SnippetId>?) {
        println("loaded")
        buffer = arrayOfNulls(snippetIds?.size ?: 0)
        setState { it.copy(position = null, snippetIds = snippetIds) }
    }

    fun setFinished(position: Int) {
        val nextPosition = position + 1
        if (nextPosition >= (stateNow.snippetIds?.size ?: 0)) return
        setState { it.copy(position = nextPosition )}
        if (stateNow.isPlaying) {
            bufferNext(nextPosition + 1)
        }
    }

    suspend fun getPath(position: Int): String? {
        if (position >= buffer.size) return null
        val path = buffer[position]?.takeIf { it.isNotEmpty() }
        if (path != null) return path
        while (stateNow.isPlaying) {
            val bufferedPath = bufferPosition(position)
            if (bufferedPath != null) return bufferedPath
            delay(100)
        }
        return null
    }

    private fun bufferNext(position: Int, count: Int = 2) {
        viewModelScope.launch(Dispatchers.IO) {
            for (i in 0 until count) {
                launch {
                    bufferPosition(position + i)
                }
            }
        }
    }

    private suspend fun bufferPosition(position: Int): String? {
        val snippetIds = stateNow.snippetIds?.takeIf { it.size > position } ?: return null
        val path = buffer[position]
        if (path != null) return path.takeIf { it.isNotEmpty() }
        buffer[position] = ""
        val snippetId = snippetIds[position]
        val audio = snippetClient.readAudioById(snippetId)
        buffer[position] = audio?.path
        return audio?.path
    }
}

data class SnippetAudioPlayerState(
    val isPlaying: Boolean = false,
    val snippetIds: List<SnippetId>? = null,
    val position: Int? = null,
)