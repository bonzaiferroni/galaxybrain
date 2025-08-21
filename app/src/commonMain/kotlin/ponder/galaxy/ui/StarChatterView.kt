package ponder.galaxy.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.PlayerPause
import compose.icons.tablericons.PlayerPlay
import kabinet.utils.toMetricString
import kabinet.utils.toShortDescription
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.Clock
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.SnippetId
import ponder.galaxy.model.data.StarId
import pondui.ui.behavior.MagicItem
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
import pondui.ui.controls.TextButton
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
    var playingId by remember { mutableStateOf<SnippetId?>(null) }
    var playSnippetIds by remember { mutableStateOf<ImmutableList<SnippetId>?>(null)}
    val now = Clock.System.now()
    Box(modifier = Modifier.fillMaxSize()) {
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
                                    val isPlaying = snippet.snippetId == playingId
                                    Text(snippet.text, modifier = Modifier.selected(isPlaying))
                                }
                            }
                            Row(1) {
                                TextButton("audio") { playSnippetIds = snippets.map { it.snippetId}.toPersistentList() }
                            }
                        }
                    }
                }
            }

            provideBottomPadding(padding)
        }
        Row(
            gap = 1,
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            SnippetAudioPlayerView(playSnippetIds) { playingId = it }
        }
    }
}