package ponder.galaxy

import kotlinx.serialization.Serializable
import ponder.galaxy.model.data.StarId
import pondui.ui.nav.AppRoute
import pondui.ui.nav.IdRoute
import pondui.ui.nav.matchLongIdRoute
import pondui.ui.nav.matchStringIdRoute

@Serializable
object StartRoute : AppRoute("Start")

@Serializable
object HelloRoute : AppRoute("Hello")

@Serializable
object ExampleListRoute : AppRoute("Examples")

@Serializable
object AppSettingsRoute : AppRoute("Settings")

@Serializable
object RedditFeedRoute : AppRoute("Reddit")

@Serializable
data class StarProfileRoute(val starId: String) : IdRoute<String>("Star", starId) {
    companion object {
        const val TITLE = "Star"
        fun matchRoute(path: String) = matchStringIdRoute(path, TITLE) { StarProfileRoute(it) }
    }
}

@Serializable
data class ExampleProfileRoute(val exampleId: Long) : AppRoute(TITLE) {
    companion object {
        const val TITLE = "Example"
        fun matchRoute(path: String) = matchLongIdRoute(path, TITLE) { ExampleProfileRoute(it) }
    }
}