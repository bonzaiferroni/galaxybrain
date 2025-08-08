package ponder.galaxy.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kabinet.utils.format
import kabinet.utils.toTimeFormat
import kotlinx.datetime.Instant
import ponder.galaxy.StarProfileRoute
import ponder.galaxy.model.data.StarId
import pondui.ui.charts.AxisConfig
import pondui.ui.charts.AxisSide
import pondui.ui.charts.BottomAxisAutoConfig
import pondui.ui.charts.ChartBox
import pondui.ui.charts.LineChart
import pondui.ui.charts.LineChartArray
import pondui.ui.charts.LineChartConfig
import pondui.ui.charts.SideAxisAutoConfig
import pondui.ui.charts.TimeChart
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Section
import pondui.ui.controls.Text
import pondui.ui.theme.Pond

@Composable
fun StarProfileScreen(
    route: StarProfileRoute,
    viewModel: StarProfileModel = viewModel { StarProfileModel(StarId(route.starId)) }
) {
    val state by viewModel.stateFlow.collectAsState()

    val star = state.star ?: return
    val starLog = state.starLogs.lastOrNull() ?: return

    Scaffold {
        Text(star.title)

        ChartBox("Chart") {
            LineChart(
                config = LineChartConfig(
                    arrays = listOf(
                        LineChartArray(
                            values = state.starLogs,
                            color = Pond.colors.swatches[0],
                            label = "Visibility",
                            axis = SideAxisAutoConfig(3, AxisSide.Left),
                            isBezier = false,
                        ) { it.visibility.toDouble() },
                        LineChartArray(
                            values = state.starLogs,
                            color = Pond.colors.swatches[1],
                            label = "Comments",
                            axis = SideAxisAutoConfig(3, AxisSide.Right),
                            isBezier = false,
                        ) { it.commentCount.toDouble() },
                        LineChartArray(
                            values = state.starLogs,
                            color = Pond.colors.swatches[2],
                            label = "Rise",
                            axis = SideAxisAutoConfig(3, AxisSide.Right),
                            isBezier = false,
                        ) { it.getRise(star.createdAt, state.riseFactor).toDouble() },
                        LineChartArray(
                            values = state.starLogs,
                            color = Pond.colors.swatches[3],
                            label = "Votes",
                            axis = SideAxisAutoConfig(3, AxisSide.Left),
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
            Text(text = "Visibility: ${star.visibility}", color = Pond.colors.swatches[0])
            Text(text = "Comments: ${starLog.commentCount}", color = Pond.colors.swatches[1])
            Text(text = "Rise: ${starLog.getRise(star.createdAt, state.riseFactor).format(1)}", color = Pond.colors.swatches[2])
            Text(text = "Votes: ${starLog.voteCount}", color = Pond.colors.swatches[3])
        }
    }
}