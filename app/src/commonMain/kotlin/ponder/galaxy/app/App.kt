package ponder.galaxy.app

import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import ponder.galaxy.app.db.AppDatabase
import ponder.galaxy.app.io.ApiClients
import ponder.galaxy.app.io.ProbeService

import pondui.ui.core.PondApp
import pondui.ui.nav.NavRoute
import pondui.ui.theme.ProvideTheme

@Composable
@Preview
fun App(
    changeRoute: (NavRoute) -> Unit,
    exitApp: (() -> Unit)?,
) {
    ProvideTheme {
//        ProvideUserContext {
//
//        }
        LaunchedEffect(Unit) {
            globalProbeService.start()
        }

        PondApp(
            config = appConfig,
            changeRoute = changeRoute,
            exitApp = exitApp
        )

    }
}

var db: AppDatabase? = null
val appDb: AppDatabase get() = db ?: error("You must initialize the database")
val globalProbeService = ProbeService()

val appApi = ApiClients()