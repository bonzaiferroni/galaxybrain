package ponder.galaxy.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.controls.Column
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Text

@Composable
fun RedditFeedScreen(
    viewModel: RedditFeedModel = viewModel { RedditFeedModel() }
) {
    val state by viewModel.stateFlow.collectAsState()

    Scaffold {
        Column(1) {
            for (post in state.posts) {
                Text(post.title)
            }
        }
    }
}