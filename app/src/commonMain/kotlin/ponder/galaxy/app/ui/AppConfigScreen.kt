package ponder.galaxy.app.ui

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.controls.Column
import pondui.ui.controls.Scaffold

@Composable
fun AppConfigScreen(
    viewModel: AppConfigModel = viewModel { AppConfigModel() }
) {
    val state by viewModel.stateFlow.collectAsState()

    Scaffold {
        Column(
            gap = 2,
            modifier = Modifier.widthIn(max = 400.dp)
        ) {

        }
    }
}