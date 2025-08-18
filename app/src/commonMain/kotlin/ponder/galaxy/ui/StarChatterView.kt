package ponder.galaxy.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.PlayerPause
import compose.icons.tablericons.PlayerPlay
import kabinet.utils.toMetricString
import kabinet.utils.toShortDescription
import kotlinx.datetime.Clock
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.StarId
import pondui.ui.behavior.selected
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.Label
import pondui.ui.controls.LabeledValue
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.ProgressBar
import pondui.ui.controls.Row
import pondui.ui.controls.Section
import pondui.ui.controls.Text
import pondui.ui.controls.actionable
import pondui.ui.controls.provideBottomPadding
import pondui.ui.controls.provideTopPadding
import kotlin.time.Duration.Companion.days

@Composable
fun StarChatterView(
    padding: PaddingValues,
    galaxyId: GalaxyId,
    starId: StarId,
    viewModel: StarChatterModel = viewModel(key = starId.value) { StarChatterModel(galaxyId, starId) }
) {
    val state by viewModel.stateFlow.collectAsState()
    val uriHandler = LocalUriHandler.current
    val now = Clock.System.now()
    LazyColumn(1) {
        provideTopPadding(padding)

        item("idea") {
            Column(1) {
                val icon = when (state.isPlaying) {
                    true -> TablerIcons.PlayerPause
                    false -> TablerIcons.PlayerPlay
                }
                Button(icon, onClick = viewModel::toggleIsPlaying)
                IdeaView(state.currentIdea) { if (state.isPlaying) viewModel.startNextIdea() }
            }
        }

        items(state.comments) { comment ->
            val isSelected = comment.commentId == state.currentIdea?.commentId
            Section(modifier = Modifier.selected(isSelected)) {
                Column(1) {
                    Row(1) {
                        Text(comment.visibility.toMetricString())
                        Label(comment.author, modifier = Modifier.weight(1f))
                        val age = now - comment.createdAt
                        val ageRatio = (age / 1.days).toFloat()
                        comment.depth?.let {
                            if (it > 0) LabeledValue("depth", it)
                        }
                        ProgressBar(
                            progress = ageRatio,
                            padding = PaddingValues(0.dp),
                            modifier = Modifier.actionable { uriHandler.openUri(comment.permalink) }
                        ) {
                            Text(age.toShortDescription())
                        }
                    }
                    val snippets = state.snippets[comment.commentId]
                    snippets?.let {
                        Column(1) {
                            snippets.forEach { snippet ->
                                Text(snippet.text)
                            }
                        }
                    }
                }
            }
        }

        provideBottomPadding(padding)
    }
}