package ponder.galaxy

import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import ponder.galaxy.db.AppDatabase

import pondui.io.ProvideUserContext
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

        PondApp(
            config = appConfig,
            changeRoute = changeRoute,
            exitApp = exitApp
        )
    }
}

var db: AppDatabase? = null
val appDb: AppDatabase get() = db ?: error("You must initialize the database")