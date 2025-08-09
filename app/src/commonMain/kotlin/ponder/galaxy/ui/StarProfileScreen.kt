package ponder.galaxy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kabinet.utils.format
import kabinet.utils.toMetricString
import kabinet.utils.toTimeFormat
import kotlinx.datetime.Instant
import ponder.galaxy.StarProfileRoute
import ponder.galaxy.model.data.StarId
import ponder.galaxy.ui.toColor
import pondui.ui.charts.AxisConfig
import pondui.ui.charts.AxisSide
import pondui.ui.charts.BottomAxisAutoConfig
import pondui.ui.charts.ChartBox
import pondui.ui.charts.LineChart
import pondui.ui.charts.LineChartArray
import pondui.ui.charts.LineChartConfig
import pondui.ui.charts.SideAxisAutoConfig
import pondui.ui.charts.TimeChart
import pondui.ui.controls.Column
import pondui.ui.controls.FlowRow
import pondui.ui.controls.H1
import pondui.ui.controls.H3
import pondui.ui.controls.LabeledValue
import pondui.ui.controls.LazyScaffold
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Section
import pondui.ui.controls.Tab
import pondui.ui.controls.Tabs
import pondui.ui.controls.Text
import pondui.ui.controls.TopBarSpacer
import pondui.ui.controls.actionable
import pondui.ui.theme.Pond
import pondui.utils.darken
import pondui.utils.mixWith

@Composable
fun StarProfileScreen(
    route: StarProfileRoute,
    viewModel: StarProfileModel = viewModel { StarProfileModel(StarId(route.starId)) }
) {
    val state by viewModel.stateFlow.collectAsState()
    val uriHandler = LocalUriHandler.current
    val colors = Pond.colors

    val star = state.star ?: return
    val galaxy = state.galaxy ?: return
    val starLog = state.starLogs.lastOrNull() ?: return

    Column {
        Column(
            modifier = Modifier.background(Pond.colors.void.darken(.1f).mixWith(Pond.colors.background, .1f))
                .padding(Pond.ruler.unitPadding)
        ) {
            TopBarSpacer()

            H3(star.title, maxLines = 2)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LabeledValue("visibility", (star.visibility).toMetricString())
                LabeledValue(
                    "rise",
                    starLog.getRise(star.createdAt, state.riseFactor).toMetricString()
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

        Tabs(headerShape = Pond.ruler.shroomDown) {
            Tab("Content", modifier = Modifier.padding(horizontal = Pond.ruler.unitSpacing)) {
                Text(star.title)
                star.thumbnailUrl?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth()
                            .clip(Pond.ruler.unitCorners)
                    )
                }
            }
            Tab("Data", modifier = Modifier.padding(horizontal = Pond.ruler.unitSpacing)) {
                ChartBox("Chart") {
                    LineChart(
                        config = LineChartConfig(
                            arrays = listOf(
                                LineChartArray(
                                    values = state.starLogs,
                                    color = Pond.colors.swatches[0],
                                    label = "Visibility",
                                    axis = SideAxisAutoConfig(5, AxisSide.Left),
                                    isBezier = false,
                                ) { it.visibility.toDouble() },
                                LineChartArray(
                                    values = state.starLogs,
                                    color = Pond.colors.swatches[1],
                                    label = "Comments",
                                    axis = SideAxisAutoConfig(5, AxisSide.Right),
                                    isBezier = false,
                                ) { it.commentCount.toDouble() },
                                LineChartArray(
                                    values = state.starLogs,
                                    color = Pond.colors.swatches[2],
                                    label = "Rise",
                                    axis = SideAxisAutoConfig(5, AxisSide.Right),
                                    isBezier = false,
                                ) { it.getRise(star.createdAt, state.riseFactor).toDouble() },
                                LineChartArray(
                                    values = state.starLogs,
                                    color = Pond.colors.swatches[3],
                                    label = "Votes",
                                    axis = SideAxisAutoConfig(5, AxisSide.Left),
                                    isBezier = false,
                                ) { it.voteCount.toDouble() },
                            ),
                            contentColor = Pond.localColors.content,
                            provideLabelX = { Instant.fromEpochSeconds(it.toLong()).toTimeFormat(false) },
                            bottomAxis = BottomAxisAutoConfig(5),
                        ) { it.createdAt.epochSeconds.toDouble() },
                        modifier = Modifier.fillMaxWidth()
                            .height(300.dp)
                    )
                }
                Section {
                    FlowRow(1, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Visibility: ${star.visibility.toMetricString()}", color = Pond.colors.swatches[0])
                        Text(text = "Comments: ${starLog.commentCount.toFloat().toMetricString()}", color = Pond.colors.swatches[1])
                        Text(text = "Rise: ${starLog.getRise(star.createdAt, state.riseFactor).toMetricString()}", color = Pond.colors.swatches[2])
                        Text(text = "Votes: ${starLog.voteCount.toFloat().toMetricString()}", color = Pond.colors.swatches[3])
                    }
                }
            }
            Tab("Comments", modifier = Modifier.padding(horizontal = Pond.ruler.unitSpacing)) {

            }
        }
    }
}