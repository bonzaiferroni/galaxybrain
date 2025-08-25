package ponder.galaxy.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kabinet.utils.toMetricString
import ponder.galaxy.app.QuestionProfileRoute
import ponder.galaxy.model.data.QuestionId
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.H3
import pondui.ui.controls.LazyScaffold
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.theme.Pond

@Composable
fun QuestionProfileScreen(
    route: QuestionProfileRoute,
    viewModel: QuestionProfileModel = viewModel { QuestionProfileModel(QuestionId(route.questionId)) }
) {
    val state by viewModel.stateFlow.collectAsState()

    val question = state.question ?: return

    LazyScaffold {
        item("QuestionProfileHeader") {
            Column(1) {
                Text(question.text)
                Row(1) {
                    TextField(
                        text = state.newDefinition,
                        label = "New Universe",
                        placeholder = "Definition",
                        onTextChanged = viewModel::setUniverseText,
                        modifier = Modifier.weight(1f)
                    )
                    Button(text = "Create", onClick = viewModel::createUniverse)
                }
            }
        }

        items(state.universes) { universe ->
            val scans = state.scans[universe.universeId]
            Row(1) {
                Column(1) {
                    H3(universe.label)
                    Text(universe.definition)
                    Column(1, modifier = Modifier.padding(start = Pond.ruler.unitSpacing)) {
                        scans?.forEach {
                            val average = it.sum / (it.count.takeIf { it > 0 } ?: 1)
                            Text("Avg signal: ${average.toMetricString()} Count: ${it.count}")
                        }
                    }
                }
            }
        }
    }
}
