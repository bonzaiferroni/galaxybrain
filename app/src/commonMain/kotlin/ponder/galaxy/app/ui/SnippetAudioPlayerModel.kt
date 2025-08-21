package ponder.galaxy.app.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ponder.galaxy.app.io.SnippetApiClient
import ponder.galaxy.model.data.SnippetId
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel

class SnippetAudioPlayerModel(
    private val snippetClient: SnippetApiClient = SnippetApiClient()
): StateModel<SnippetAudioPlayerState>() {
    override val state = ModelState(SnippetAudioPlayerState())

    private var buffer: Array<String?> = emptyArray()
    private var snippetIds: List<SnippetId>? = null

    fun toggleIsPlaying(isPlaying: Boolean = !stateNow.isPlaying) {
        if (isPlaying) {
            val position = stateNow.position ?: 0
            setState { it.copy(isPlaying = true, position = position) }
            bufferNext(position)
        } else {
            setState { it.copy(isPlaying = false)}
        }
    }

    fun load(loadIds: List<SnippetId>?) {
        buffer = arrayOfNulls(loadIds?.size ?: 0)
        snippetIds = loadIds
        setState { it.copy(position = null, isPlaying = false) }
    }

    fun setPosition(position: Int) {
        if (position < 0 || position >= (snippetIds?.size ?: 0)) return
        setState { it.copy(position = position )}
        if (stateNow.isPlaying) {
            bufferNext(position + 1)
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
        val snippetIds = snippetIds?.takeIf { it.size > position } ?: return null
        val path = buffer[position]
        if (path != null) return path.takeIf { it.isNotEmpty() }
        buffer[position] = ""
        val snippetId = snippetIds[position]
        val audio = snippetClient.readAudioById(snippetId)
        buffer[position] = audio?.path ?: error("unable to download audio")
        return audio.path
    }
}

data class SnippetAudioPlayerState(
    val isPlaying: Boolean = false,
    val position: Int? = null,
)