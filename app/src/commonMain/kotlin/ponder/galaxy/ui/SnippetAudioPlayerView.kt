package ponder.galaxy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.PlayerPause
import compose.icons.tablericons.PlayerPlay
import compose.icons.tablericons.PlayerTrackNext
import compose.icons.tablericons.PlayerTrackPrev
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ponder.galaxy.model.data.SnippetId
import pondui.APP_API_URL
import pondui.WavePlayer
import pondui.ui.controls.Button
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.theme.Pond

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

    if (snippetIds == null) return

    LaunchedEffect(state.position) {
        if (state.isPlaying) {
            state.position?.let { position ->
                val audio = viewModel.getPath(position)
                val snippetId = snippetIds.getOrNull(position)
                if (audio != null && snippetId != null) {
                    onPlayId?.invoke(snippetId)
                    wavePlayer.play("$APP_API_URL/$audio")
                    if (isActive) {
                         viewModel.setPosition(position + 1)
                    }
                }
            }
        }
    }

    Row(
        gap = 1,
        modifier = Modifier.clip(Pond.ruler.pill)
            .background(Pond.colors.void)
            .padding(Pond.ruler.unitPadding)
    ) {
        val position = state.position
        if (state.isPlaying) {
            Button(
                imageVector = TablerIcons.PlayerTrackPrev,
                isEnabled = position != null && position > 0
            ) { position?.let { viewModel.setPosition(it - 1) } }
            if (position != null) {
                Text("${position + 1} of ${snippetIds.size}")
            }
            Button(TablerIcons.PlayerPause, onClick = viewModel::toggleIsPlaying)
            Button(
                imageVector = TablerIcons.PlayerTrackNext,
                isEnabled = position != null && position < snippetIds.size
            ) { position?.let { viewModel.setPosition(it + 1) } }
        } else {
            Button(TablerIcons.PlayerPlay, onClick = viewModel::toggleIsPlaying)
        }
    }
}