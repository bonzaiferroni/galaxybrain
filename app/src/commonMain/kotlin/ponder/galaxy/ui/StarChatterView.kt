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
import kabinet.utils.toMetricString
import kabinet.utils.toShortDescription
import kotlinx.datetime.Clock
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.StarId
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

        items(state.comments) { chatter ->
            Section {
                Column(1) {
                    Row(1) {
                        Text(chatter.visibility.toMetricString())
                        Label(chatter.author, modifier = Modifier.weight(1f))
                        val age = now - chatter.createdAt
                        val ageRatio = (age / 1.days).toFloat()
                        chatter.depth?.let {
                            if (it > 0) LabeledValue("depth", it)
                        }
                        ProgressBar(
                            progress = ageRatio,
                            padding = PaddingValues(0.dp),
                            modifier = Modifier.actionable { uriHandler.openUri(chatter.permalink) }
                        ) {
                            Text(age.toShortDescription())
                        }
                    }
                    Text(chatter.text)
                }
            }
        }

        provideBottomPadding(padding)
    }
}