package ponder.galaxy.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.PlayerPlay
import kotlinx.collections.immutable.ImmutableList
import ponder.galaxy.model.data.SnippetId
import pondui.APP_API_URL
import pondui.WavePlayer
import pondui.ui.controls.Button

@Composable
fun SnippetAudioPlayerView(
    snippetIds: ImmutableList<SnippetId>?,
    viewModel: SnippetAudioPlayerModel = viewModel { SnippetAudioPlayerModel() },
    onPlayId: ((SnippetId?) -> Unit)? = null,
) {
    val state by viewModel.stateFlow.collectAsState()

    LaunchedEffect(snippetIds) {
        viewModel.load(snippetIds)
    }

    val wavePlayer = remember { WavePlayer() }

    LaunchedEffect(state.position) {
        if (state.isPlaying) {
            state.position?.let { position ->
                val audio = viewModel.getPath(position)
                val snippetId = state.snippetIds?.getOrNull(position)
                if (audio != null && snippetId != null) {
                    onPlayId?.invoke(snippetId)
                    wavePlayer.play("$APP_API_URL/$audio")
                    viewModel.setFinished(position)
                }
            }
        }
    }

    val playingIds = state.snippetIds ?: return
    if (state.isPlaying) {
        Button("${state.position} of ${playingIds.size}", onClick = viewModel::toggleIsPlaying)
    } else {
        Button(TablerIcons.PlayerPlay, onClick = viewModel::toggleIsPlaying)
    }
}