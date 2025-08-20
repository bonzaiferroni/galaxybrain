package ponder.galaxy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kabinet.utils.toMetricString
import kabinet.utils.toShortDescription
import kabinet.utils.toTimeFormat
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.galaxy.StarProfileRoute
import ponder.galaxy.model.data.SnippetId
import ponder.galaxy.model.data.StarId
import pondui.APP_API_URL
import pondui.WavePlayer
import pondui.ui.behavior.selected
import pondui.ui.charts.AxisSide
import pondui.ui.charts.BottomAxisAutoConfig
import pondui.ui.charts.ChartBox
import pondui.ui.charts.LineChart
import pondui.ui.charts.LineChartArray
import pondui.ui.charts.LineChartConfig
import pondui.ui.charts.SideAxisAutoConfig
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.DrawerScaffold
import pondui.ui.controls.FlowRow
import pondui.ui.controls.H3
import pondui.ui.controls.LabeledValue
import pondui.ui.controls.ProgressBar
import pondui.ui.controls.Row
import pondui.ui.controls.Section
import pondui.ui.controls.Tab
import pondui.ui.controls.TabContent
import pondui.ui.controls.TabHeader
import pondui.ui.controls.TabScope
import pondui.ui.controls.Text
import pondui.ui.controls.actionable
import pondui.ui.controls.scaffoldPadding
import pondui.ui.nav.LocalNav
import pondui.ui.theme.Pond

@Composable
fun StarProfileScreen(
    route: StarProfileRoute,
    viewModel: StarProfileModel = viewModel { StarProfileModel(StarId(route.starId)) }
) {
    val state by viewModel.stateFlow.collectAsState()
    val uriHandler = LocalUriHandler.current
    val colors = Pond.colors
    val tabScope = remember { TabScope() }

    val star = state.star ?: return
    val galaxy = state.galaxy ?: return
    val starLog = state.starLogs.lastOrNull()
    val nav = LocalNav.current
    val wavePlayer = remember { WavePlayer() }

    LaunchedEffect(state.isPlaying) {
        if (state.isPlaying) {
            state.contentIdea?.let {
                wavePlayer.play("$APP_API_URL/${it.audioUrl}")
            }
        }
    }

    val launcher = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = listOf("html"))
    ) { file ->
        val html = file?.file?.readText()
        if (html != null) {
            viewModel.readFromHtml(html)
        }
    }

    DrawerScaffold(
        drawerContent = {
            H3(star.displayTitle, maxLines = 2, modifier = Modifier.actionable { uriHandler.openUri(star.url) })
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(galaxy.name, color = galaxy.toColor(colors), modifier = Modifier.actionable { uriHandler.openUri(galaxy.url)})
                LabeledValue(
                    "comments",
                    star.commentCount,
                    modifier = Modifier.actionable { uriHandler.openUri(star.url) })
                val now = Clock.System.now()
                val age = now - star.createdAt
                val ageRatio = age.inWholeSeconds / (60 * 60 * 24).toFloat()
                ProgressBar(
                    progress = ageRatio,
                ) {
                    Text(age.toShortDescription())
                }
            }
            TabHeader(
                scope = tabScope,
                headerShape = RoundedCornerShape(
                    topStart = Pond.ruler.unitSpacing,
                    topEnd = Pond.ruler.unitSpacing,
                    bottomStart = Pond.ruler.defaultCorner,
                    bottomEnd = Pond.ruler.defaultCorner
                )
            )
        }
    ) { padding ->
        TabContent(tabScope) {
            Tab("Content", modifier = Modifier.verticalScroll(rememberScrollState())) {
                Section(modifier = Modifier.scaffoldPadding(padding).fillMaxWidth()) {
                    var playingId by remember { mutableStateOf<SnippetId?>(null) }
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
                    Column(1) {
                        state.snippets.forEach { snippet ->
                            val links = state.outgoingLinks.filter { it.snippetId == snippet.snippetId }
                            val isPlaying = snippet.snippetId == playingId
                            SnippetText(
                                text = snippet.text,
                                starLinks = links,
                                modifier = Modifier.background(Color.White.copy(.1f)).selected(isPlaying)
                            ) { println("ey") }
                        }
                    }
                    Row(1) {
                        val snippetIds = remember(state.snippets) {
                            state.snippets.takeIf { it.isNotEmpty() }?.map { it.snippetId }?.toPersistentList()
                        }
                        SnippetAudioPlayerView(snippetIds) { playingId = it }
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
            Tab("Links", modifier = Modifier.scaffoldPadding(padding)) {
                val starLinks = remember(state.outgoingLinks) { state.outgoingLinks.filter { it.commentId == null } }
                if (starLinks.isNotEmpty()) {
                    H3("Content links")
                }
                for (starLink in starLinks) {
                    Text(starLink.url.href)
                }
                val commentLinks = remember(state.outgoingLinks) { state.outgoingLinks.filter { it.commentId != null } }
                if (commentLinks.isNotEmpty()) {
                    H3("Comment links")
                }
                for (starLink in commentLinks) {
                    Text(starLink.url.href)
                }
            }
            Tab("Data", modifier = Modifier.scaffoldPadding(padding)) {
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
                starLog?.let {
                    Section {
                        FlowRow(1, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Visibility: ${star.visibility?.toMetricString()}", color = Pond.colors.swatches[0])
                            Text(text = "Comments: ${starLog.commentCount.toFloat().toMetricString()}", color = Pond.colors.swatches[1])
                            Text(text = "Rise: ${starLog.getRise(star.createdAt, state.riseFactor).toMetricString()}", color = Pond.colors.swatches[2])
                            Text(text = "Votes: ${starLog.voteCount.toFloat().toMetricString()}", color = Pond.colors.swatches[3])
                        }
                    }
                }
            }
            Tab("Comments") {
                StarChatterView(padding, galaxy.galaxyId, star.starId)
            }
        }
    }
}
