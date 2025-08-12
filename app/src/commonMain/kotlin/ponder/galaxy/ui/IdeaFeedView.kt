package ponder.galaxy.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.APP_API_URL
import pondui.WavePlayer

@Composable
fun IdeaFeedView(
    viewModel: IdeaFeedModel = viewModel { IdeaFeedModel() }
) {
    val state by viewModel.stateFlow.collectAsState()
    val speechUrl = state.speechUrl
    val wavePlayer = remember { WavePlayer() }
    LaunchedEffect(speechUrl) {
        if (speechUrl == null) return@LaunchedEffect
        wavePlayer.play("$APP_API_URL/$speechUrl")
    }
}