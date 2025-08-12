package ponder.galaxy.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kabinet.utils.toShortDescription
import kotlinx.datetime.Clock
import pondui.APP_API_URL
import pondui.WavePlayer
import pondui.ui.behavior.MagicItem
import pondui.ui.controls.Column
import pondui.ui.controls.Drawer
import pondui.ui.controls.ProgressBar
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.theme.Pond
import kotlin.time.Duration.Companion.days

@Composable
fun IdeaFeedView(
    isIdeaVisible: Boolean,
    viewModel: IdeaFeedModel = viewModel { IdeaFeedModel() }
) {
    val state by viewModel.stateFlow.collectAsState()
    val audioUrl = state.idea?.audioUrl
    val wavePlayer = remember { WavePlayer() }
    LaunchedEffect(audioUrl) {
        if (audioUrl == null) return@LaunchedEffect
        wavePlayer.play("$APP_API_URL/$audioUrl")
    }

    Drawer(
        isIdeaVisible
    ) {
        MagicItem(state, offsetY = 100.dp) { currentState ->
            val star = currentState.star ?: return@MagicItem
            val galaxy = currentState.galaxy ?: return@MagicItem
            val idea = currentState.idea ?: return@MagicItem
            Column(1) {
                idea.imageUrl?.let { imageUrl ->
                    AsyncImage(model = "$APP_API_URL/$imageUrl", contentDescription = null)
                }
                Text(star.title)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(galaxy.name, color = Pond.colors.getSwatchFromIndex(galaxy.intrinsicIndex))
                    val now = Clock.System.now()
                    val age = now - star.createdAt
                    val ageRatio = (age / 1.days).toFloat()
                    ProgressBar(ageRatio) {
                        Text(age.toShortDescription())
                    }
                }
            }
        }
    }
}