package ponder.galaxy.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.behavior.drawLabel
import pondui.ui.controls.Scaffold
import pondui.ui.controls.TextField

@Composable
fun AppSettingsScreen(
    viewModel: AppSettingsModel = viewModel { AppSettingsModel() }
) {
    val state by viewModel.stateFlow.collectAsState()

    Scaffold {
        TextField(
            text = state.redditUsername,
            onTextChanged = viewModel::setRedditUsername,
            label = "reddit username",
        )
        TextField(
            text = state.redditPassword,
            onTextChanged = viewModel::setRedditPassword,
            label = "reddit password",
        )
        TextField(
            text = state.redditAppId,
            onTextChanged = viewModel::setRedditAppId,
            label = "reddit client id",
        )
        TextField(
            text = state.redditAppSecret,
            onTextChanged = viewModel::setRedditAppSecret,
            label = "reddit client secret",
        )
    }
}