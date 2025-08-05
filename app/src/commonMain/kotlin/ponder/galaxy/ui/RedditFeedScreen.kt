package ponder.galaxy.ui

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kabinet.utils.toMetricString
import pondui.ui.controls.Column
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.Row
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Section
import pondui.ui.controls.Text

@Composable
fun RedditFeedScreen(
    viewModel: RedditFeedModel = viewModel { RedditFeedModel() }
) {
    val state by viewModel.stateFlow.collectAsState()

    Scaffold {
        LazyColumn(1) {
            items(state.stars) { star ->
                val starLogs = state.starLogMap[star.starId] ?: return@items
                val latestStarLog = starLogs.lastOrNull() ?: return@items
                Section {
                    Column(1) {
                        Row(1) {
                            Text("${star.visibility.toMetricString()}:")
                            Text(star.title, modifier = Modifier.weight(1f))
                            star.thumbnailUrl?.let {
                                AsyncImage(model = it, contentDescription = null)
                            }
                        }
                        Row(1) {
                            Text("rise: ${latestStarLog.rise.toMetricString()} comments: ${star.commentCount}")
                        }
                    }
                }
            }
        }
    }
}