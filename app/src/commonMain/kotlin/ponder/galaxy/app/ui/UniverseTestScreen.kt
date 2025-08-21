package ponder.galaxy.app.ui

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kabinet.utils.toMetricString
import pondui.ui.controls.Button
import pondui.ui.controls.LazyScaffold
import pondui.ui.controls.Row
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Section
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.controls.actionable

@Composable
fun UniverseTestScreen(
    viewModel: UniverseTestModel = viewModel { UniverseTestModel() }
) {
    val state by viewModel.stateFlow.collectAsState()
    LazyScaffold {
        item("header") {
            Row(1) {
                TextField(
                    text = state.universe,
                    onTextChanged = viewModel::setUniverse,
                    modifier = Modifier.weight(1f)
                )
                Button("Test", onClick = viewModel::testUniverse)
            }
        }

        items(state.tests) { test ->
            Section() {
                Row(1, modifier = Modifier.actionable {
                    viewModel.setUniverse(test.snippet.text)
                    viewModel.testUniverse()
                }) {
                    Text(test.distance.toString(), modifier = Modifier.width(100.dp))
                    Text(test.snippet.text)
                }
            }
        }
    }
}
