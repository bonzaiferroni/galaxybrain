package ponder.galaxy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kabinet.utils.toTimeFormat
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.galaxy.StarProfileRoute
import ponder.galaxy.model.data.Galaxy
import pondui.ui.charts.AxisSide
import pondui.ui.charts.BottomAxisAutoConfig
import pondui.ui.charts.ChartBox
import pondui.ui.charts.LineChart
import pondui.ui.charts.LineChartArray
import pondui.ui.charts.LineChartConfig
import pondui.ui.charts.SideAxisAutoConfig
import pondui.ui.controls.Button
import pondui.ui.controls.ButtonToggle
import pondui.ui.controls.Column
import pondui.ui.controls.Drawer
import pondui.ui.controls.FlowRow
import pondui.ui.controls.Icon
import pondui.ui.controls.IntegerWheel
import pondui.ui.controls.Label
import pondui.ui.controls.LabeledValue
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.ProgressBar
import pondui.ui.controls.Row
import pondui.ui.controls.Section
import pondui.ui.controls.Text
import pondui.ui.controls.TitleCloud
import pondui.ui.controls.TopBarSpacer
import pondui.ui.controls.actionable
import pondui.ui.controls.bottomBarSpacerItem
import pondui.ui.theme.Pond
import pondui.ui.theme.PondColors

@Composable
fun GalaxyFeedScreen(
    viewModel: GalaxyFeedModel = viewModel { GalaxyFeedModel() }
) {
    val state by viewModel.stateFlow.collectAsState()
    val uriHandler = LocalUriHandler.current
    val colors = Pond.colors
    var isChartVisible by remember { mutableStateOf(false) }
    var topY by remember (isChartVisible) { mutableStateOf(1.0) }
    val topGalaxy = state.galaxies.maxByOrNull { it.visibility }

    TitleCloud("Active Galaxies", state.isGalaxyCloudVisible, viewModel::toggleGalaxyCloud) {
        Column(1) {
            IntegerWheel(
                value = state.riseFactor,
                minValue = 1,
                maxValue = 100,
                label = "Rise",
                onValueChanged = viewModel::setRiseFactor
            )
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
    }

    val lazyListState = rememberLazyListState()

    val firstVisibleIndex by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex }
    }

    Column(1) {
        TopBarSpacer()

        Drawer(
            isOpen = isChartVisible,
            height = 200.dp,
        ) {
            val firstVisibleStarId = state.stars.takeIf { it.size > firstVisibleIndex }
                ?.let { it[firstVisibleIndex].starId }
            val chartConfig = remember(firstVisibleStarId, state.isNormalized) {
                val stars = viewModel.getStarsAfterIndex(firstVisibleIndex)
                val arrays = stars.mapNotNull { star ->
                    val galaxy = state.galaxies.firstOrNull { it.galaxyId == star.galaxyId } ?: return@mapNotNull null
                    val starLogs = viewModel.getStarLogs(star.starId) ?: return@mapNotNull null
                    val scale =
                        if (state.isNormalized && topGalaxy != null) topGalaxy.visibility / galaxy.visibility else 1f
                    LineChartArray(
                        values = starLogs,
                        color = galaxy.toColor(colors),
                        isBezier = false,
                        axis = SideAxisAutoConfig(5, AxisSide.Left, colors.contentSky),
                        floor = 0.0,
                        ceiling = topY
                    ) { (it.visibility * scale).toDouble().also { rise -> if (rise > topY) topY = rise } }
                }
                LineChartConfig(
                    arrays = arrays,
                    contentColor = colors.contentSky,
                    provideLabelX = { Instant.fromEpochSeconds(it.toLong()).toTimeFormat(false) },
                    bottomAxis = BottomAxisAutoConfig(5),
                ) { it.createdAt.epochSeconds.toDouble() }
            }

            ChartBox("Chart") {
                LineChart(
                    config = chartConfig,
                    modifier = Modifier.fillMaxWidth()
                        .height(200.dp)
                )
            }
        }

        LazyColumn(
            gap = 1,
            state = lazyListState
        ) {
            item("header") {
                Row(1) {
                    Button("Set Galaxies", onClick = viewModel::toggleGalaxyCloud)
                    ButtonToggle(isChartVisible, "Chart") { isChartVisible = !isChartVisible }
                    ButtonToggle(state.isNormalized, "Normalize", onToggle = viewModel::setNormalized)
                }
            }

            items(state.stars, key = { it.starId }) { star ->
                val starLogs = viewModel.getStarLogs(star.starId) ?: return@items
                val latestStarLog = starLogs.lastOrNull() ?: return@items
                val galaxy = state.galaxies.firstOrNull { it.galaxyId == star.galaxyId } ?: return@items
                val now = Clock.System.now()
                val scale =
                    if (state.isNormalized && topGalaxy != null) topGalaxy.visibility / galaxy.visibility else 1f

                Section(
                    modifier = Modifier.height(IntrinsicSize.Max)
                        .animateItem()
                ) {
                    Row(gap = 1, verticalAlignment = Alignment.Top) {
                        Column(1, modifier = Modifier.weight(1f)) {
                            Box(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = star.title,
                                    modifier = Modifier.actionable { uriHandler.openUri(star.link) }
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val label = when (state.isNormalized) {
                                    true -> "visibility ${scale.toMetricString()}x"
                                    false -> "visibility"
                                }
                                LabeledValue(label, (star.visibility * scale).toMetricString())
                                LabeledValue(
                                    "rise",
                                    latestStarLog.getRise(star.createdAt, state.riseFactor).toMetricString()
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(galaxy.name, color = galaxy.toColor(colors))
                                LabeledValue(
                                    "comments",
                                    star.commentCount,
                                    modifier = Modifier.actionable { uriHandler.openUri(star.permalink) })
                            }
                        }
                        Column(
                            gap = 1,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(100.dp)
                                .actionable(StarProfileRoute(star.starId.value))
                        ) {
                            Box() {
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
                            }
                            val age = now - star.createdAt
                            val ageRatio = age.inWholeSeconds / (60 * 60 * 24).toFloat()
                            ProgressBar(
                                progress = ageRatio,
                                padding = PaddingValues(0.dp)
                            ) {
                                Text(age.toShortDescription())
                            }
                        }
                    }
                }
            }

            bottomBarSpacerItem()
        }
    }
}

fun Galaxy.toColor(colors: PondColors) = colors.getSwatchFromIndex(name.length)