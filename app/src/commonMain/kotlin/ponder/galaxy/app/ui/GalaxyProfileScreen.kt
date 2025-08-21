package ponder.galaxy.app.ui

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import ponder.galaxy.app.GalaxyProfileRoute
import ponder.galaxy.app.StarProfileRoute
import ponder.galaxy.model.data.GalaxyId
import pondui.ui.controls.LazyScaffold
import pondui.ui.controls.TextButton
import pondui.ui.nav.LocalNav

@Composable
fun GalaxyProfileScreen(
    route: GalaxyProfileRoute,
    viewModel: GalaxyProfileModel = viewModel { GalaxyProfileModel(GalaxyId(route.galaxyId)) }
) {
    val state by viewModel.stateFlow.collectAsState()
    val nav = LocalNav.current

    LazyScaffold {
        items(state.stars) { star ->
            TextButton(star.displayTitle) { nav.go(StarProfileRoute(star.starId.value)) }
        }
    }
}