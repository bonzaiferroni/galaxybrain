package ponder.galaxy.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kabinet.utils.toMetricString
import kabinet.utils.toShortDescription
import kotlinx.datetime.Clock
import pondui.ui.controls.Column
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.LazyScaffold
import pondui.ui.controls.ProgressBar
import pondui.ui.controls.Row
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Section
import pondui.ui.controls.Text
import pondui.ui.controls.actionable

@Composable
fun RedditFeedScreen(
    viewModel: RedditFeedModel = viewModel { RedditFeedModel() }
) {
    val state by viewModel.stateFlow.collectAsState()
    val uriHandler = LocalUriHandler.current

    LazyScaffold {
        items(state.stars, key = { it.starId }) { star ->
            val starLogs = viewModel.getStarLogs(star.starId) ?: return@items
            val latestStarLog = starLogs.lastOrNull() ?: return@items
            Section(modifier = Modifier.animateItem()) {
                Row(gap = 1, verticalAlignment = Alignment.Top) {
                    Column(
                        gap = 1,
                        modifier = Modifier.width(50.dp)
                    ) {
                        star.thumbnailUrl?.let {
                            AsyncImage(model = it, contentDescription = null, modifier = Modifier.fillMaxWidth())
                        }
                        Text(star.visibility.toMetricString())
                    }
                    Column(1) {
                        Text(
                            text = star.title,
                            modifier = Modifier.actionable { uriHandler.openUri(star.link) }
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("rise: ${latestStarLog.rise.toMetricString()}")
                            Text(
                                text = "comments: ${star.commentCount}",
                                modifier = Modifier.actionable { uriHandler.openUri(star.permalink)}
                            )
                            val now = Clock.System.now()
                            val age = now - star.createdAt
                            val ageRatio = age.inWholeSeconds / (60 * 60 * 24).toFloat()
                            ProgressBar(ageRatio) {
                                Text(age.toShortDescription())
                            }
                        }
                    }
                }
            }
        }
    }
}