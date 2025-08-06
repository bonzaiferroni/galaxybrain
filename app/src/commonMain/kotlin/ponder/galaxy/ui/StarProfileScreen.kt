package ponder.galaxy.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import ponder.galaxy.StarProfileRoute
import ponder.galaxy.model.data.StarId
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Text

@Composable
fun StarProfileScreen(
    route: StarProfileRoute,
    viewModel: StarProfileModel = viewModel { StarProfileModel(StarId(route.starId)) }
) {
    val state by viewModel.stateFlow.collectAsState()

    val star = state.star ?: return

    Scaffold {
        Text(star.title)
    }
}