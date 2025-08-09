package ponder.galaxy.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.controls.Column
import pondui.ui.controls.Text

@Composable
fun StarChatterView(
    subredditName: String,
    articleId: String,
    viewModel: StarChatterModel = viewModel(key = articleId) { StarChatterModel(subredditName, articleId) }
) {
    val state by viewModel.stateFlow.collectAsState()
    Column(1) {
        for (chatter in state.chatters) {
            Text(chatter.body)
        }
    }
}