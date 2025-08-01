package ponder.galaxy.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.controls.Column
import pondui.ui.controls.Row
import pondui.ui.controls.Scaffold
import pondui.ui.controls.TextField

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
            Row(1) {
                TextField(
                    text = state.redditUsername,
                    onTextChanged = viewModel::setRedditUsername,
                    label = "reddit username",
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    text = state.redditPassword,
                    onTextChanged = viewModel::setRedditPassword,
                    label = "reddit password",
                    modifier = Modifier.weight(1f)
                )
            }
            TextField(
                text = state.redditAppId,
                onTextChanged = viewModel::setRedditAppId,
                label = "reddit client id",
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                text = state.redditAppSecret,
                onTextChanged = viewModel::setRedditAppSecret,
                label = "reddit client secret",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}