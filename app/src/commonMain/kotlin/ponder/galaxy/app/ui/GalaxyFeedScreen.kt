package ponder.galaxy.app.ui

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import ponder.galaxy.app.GalaxyProfileRoute
import pondui.ui.controls.LazyScaffold
import pondui.ui.controls.TextButton
import pondui.ui.nav.LocalNav

@Composable
fun GalaxyFeedScreen(
    viewModel: GalaxyFeedModel = viewModel { GalaxyFeedModel() }
) {
    val state by viewModel.stateFlow.collectAsState()
    val nav = LocalNav.current

    LazyScaffold {
        items(state.galaxies) { galaxy ->
            TextButton(galaxy.name) { nav.go(GalaxyProfileRoute(galaxy.galaxyId.value)) }
        }
    }
}