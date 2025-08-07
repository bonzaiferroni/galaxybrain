package ponder.galaxy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import compose.icons.TablerIcons
import compose.icons.tablericons.BrandReddit
import kabinet.utils.toMetricString
import kabinet.utils.toShortDescription
import kotlinx.datetime.Clock
import ponder.galaxy.StarProfileRoute
import pondui.ui.controls.Button
import pondui.ui.controls.ButtonToggle
import pondui.ui.controls.Column
import pondui.ui.controls.FlowRow
import pondui.ui.controls.Icon
import pondui.ui.controls.LazyScaffold
import pondui.ui.controls.ProgressBar
import pondui.ui.controls.Row
import pondui.ui.controls.Section
import pondui.ui.controls.Text
import pondui.ui.controls.TitleCloud
import pondui.ui.controls.actionable
import pondui.ui.theme.Pond

@Composable
fun GalaxyFeedScreen(
    viewModel: GalaxyFeedModel = viewModel { GalaxyFeedModel() }
) {
    val state by viewModel.stateFlow.collectAsState()
    val uriHandler = LocalUriHandler.current

    TitleCloud("Active Galaxies", state.isGalaxyCloudVisible, viewModel::toggleGalaxyCloud) {
        FlowRow(
            gap = 1,
            modifier = Modifier.fillMaxWidth()
        ) {
            state.galaxies.forEach { galaxy ->
                ButtonToggle(state.activeGalaxyNames.contains(galaxy.name), galaxy.name) {
                    viewModel.toggleGalaxy(galaxy.name)
                }
            }
        }
    }

    LazyScaffold {
        item("header") {
            Button("Set Galaxies", onClick = viewModel::toggleGalaxyCloud)
        }

        items(state.stars, key = { it.starId }) { star ->
            val starLogs = viewModel.getStarLogs(star.starId) ?: return@items
            val latestStarLog = starLogs.lastOrNull() ?: return@items
            val galaxy = state.galaxies.firstOrNull { it.galaxyId == star.galaxyId } ?: return@items
            val now = Clock.System.now()

            Section(modifier = Modifier.animateItem()) {
                Row(gap = 1, verticalAlignment = Alignment.Top) {
                    Column(
                        gap = 1,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(100.dp)
                            .actionable(StarProfileRoute(star.starId.value))
                    ) {
                        when (star.thumbnailUrl) {
                            null -> {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxWidth()
                                        .clip(Pond.ruler.unitCorners)
                                        .background(Color.White.copy(.1f))
                                        .padding(Pond.ruler.unitPadding)
                                ) {
                                    Icon(TablerIcons.BrandReddit)
                                }
                            }
                            else -> {
                                AsyncImage(
                                    model = star.thumbnailUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxWidth()
                                        .clip(Pond.ruler.unitCorners)
                                )
                            }
                        }
                        val age = now - star.createdAt
                        val ageRatio = age.inWholeSeconds / (60 * 60 * 24).toFloat()
                        ProgressBar(
                            progress = ageRatio,
                            padding = PaddingValues(0.dp)
                        ) {
                            Text(age.toShortDescription())
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
                            Text("rise: ${latestStarLog.getRise(star.createdAt).toMetricString()}")
                            Text(
                                text = "comments: ${star.commentCount}",
                                modifier = Modifier.actionable { uriHandler.openUri(star.permalink)}
                            )
                        }
                        Row(1) {
                            Text(galaxy.name)
                        }
                    }
                }
            }
        }
    }
}