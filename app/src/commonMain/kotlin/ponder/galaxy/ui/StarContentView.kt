package ponder.galaxy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import coil3.compose.AsyncImage
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.collections.immutable.toPersistentList
import ponder.galaxy.StarProfileRoute
import ponder.galaxy.model.data.SnippetId
import pondui.APP_API_URL
import pondui.ui.behavior.selected
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.H3
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.Row
import pondui.ui.nav.LocalNav
import pondui.ui.theme.Pond

@Composable
fun StarContentView(
    viewModel: StarProfileModel,
    padding: PaddingValues,
) {
    val state by viewModel.stateFlow.collectAsState()
    val star = state.star ?: return
    val nav = LocalNav.current
    var playingId by remember { mutableStateOf<SnippetId?>(null) }

    val launcher = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = listOf("html"))
    ) { file ->
        val html = file?.file?.readText()
        if (html != null) {
            viewModel.readFromHtml(html)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(1) {
            item("header") {
                Column(1, modifier = Modifier.padding(top = padding.calculateTopPadding())) {
                    val imgUrl = star.imageUrl ?: state.contentIdea?.imageUrl?.let { "$APP_API_URL/$it"}
                    imgUrl?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth()
                                .clip(Pond.ruler.unitCorners)
                        )
                    }
                    H3(star.displayTitle)
                }
            }

            items(state.snippets, key = { it.snippetId }) { snippet ->
                val links = state.outgoingLinks.filter { it.snippetId == snippet.snippetId }
                val isPlaying = snippet.snippetId == playingId
                SnippetText(
                    text = snippet.text,
                    starLinks = links,
                    modifier = Modifier.selected(isPlaying)
                ) { println("ey") }
            }

            item("footer") {
                Row(1, modifier = Modifier.padding(bottom = padding.calculateBottomPadding())) {
                    star.link?.let {
                        val linkStar = state.linkStar
                        if (linkStar != null) {
                            Button("Read") { nav.go(StarProfileRoute(linkStar.starId.value)) }
                        } else {
                            Button("Discover", onClick = viewModel::discoverLink)
                        }
                    }
                    if (star.title == null) {
                        Button("HTML") { launcher.launch() }
                    }
                }
            }
        }
        Row(
            gap = 1,
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            val snippetIds = remember(state.snippets) {
                state.snippets.takeIf { it.isNotEmpty() }?.map { it.snippetId }?.toPersistentList()
            }
            SnippetAudioPlayerView(snippetIds) { playingId = it }
        }
    }
}