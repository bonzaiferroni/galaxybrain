package ponder.galaxy.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kabinet.utils.toShortDescription
import kotlinx.datetime.Clock
import ponder.galaxy.model.data.Idea
import pondui.APP_API_URL
import pondui.WavePlayer
import pondui.ui.behavior.MagicItem
import pondui.ui.controls.Column
import pondui.ui.controls.Drawer
import pondui.ui.controls.ProgressBar
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.controls.actionable
import pondui.ui.theme.Pond
import kotlin.time.Duration.Companion.days

@Composable
fun IdeaFocusView(
    isIdeaVisible: Boolean,
    viewModel: IdeaFocusModel = viewModel { IdeaFocusModel() }
) {
    val state by viewModel.stateFlow.collectAsState()
    val audioUrl = state.idea?.audioUrl
    val wavePlayer = remember { WavePlayer() }
    val uriHandle = LocalUriHandler.current
    LaunchedEffect(audioUrl) {
        if (audioUrl == null) return@LaunchedEffect
        wavePlayer.play("$APP_API_URL/$audioUrl")
    }

    Drawer(isIdeaVisible, openHeight = 400.dp) {
        MagicItem(state, offsetX = 100.dp) { currentState ->
            val star = currentState.star ?: return@MagicItem
            val galaxy = currentState.galaxy ?: return@MagicItem
            val idea = currentState.idea ?: return@MagicItem
            Column(1, horizontalAlignment = Alignment.CenterHorizontally) {
                idea.imageUrl?.let { imageUrl ->
                    val url = if (imageUrl.startsWith("http")) imageUrl else "$APP_API_URL/$imageUrl"
                    AsyncImage(model = url, contentDescription = null, modifier = Modifier.weight(1f, fill = false))
                }
                Text(star.displayTitle, modifier = Modifier.actionable { uriHandle.openUri(star.url) } )
                Row(1) {
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

@Composable
fun IdeaView(
    idea: Idea?,
    onFinished: () -> Unit
) {
    val audioUrl = idea?.audioUrl
    val wavePlayer = remember { WavePlayer() }

    LaunchedEffect(audioUrl) {
        if (audioUrl == null) return@LaunchedEffect
        wavePlayer.play("$APP_API_URL/$audioUrl")
        onFinished()
    }

    MagicItem(idea) { idea ->
        Column(1, horizontalAlignment = Alignment.CenterHorizontally) {
            idea?.imageUrl?.let { imageUrl ->
                val url = if (imageUrl.startsWith("http")) imageUrl else "$APP_API_URL/$imageUrl"
                AsyncImage(model = url, contentDescription = null, modifier = Modifier.weight(1f, fill = false))
            }
            idea?.text?.let {
                Text(it)
            }
        }
    }
}