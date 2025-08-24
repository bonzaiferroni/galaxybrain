package ponder.galaxy.app

import kotlinx.serialization.Serializable
import pondui.ui.nav.AppRoute
import pondui.ui.nav.IdRoute
import pondui.ui.nav.matchLongIdRoute
import pondui.ui.nav.matchStringIdRoute

@Serializable
object StartRoute : AppRoute("Start")

@Serializable
object HelloRoute : AppRoute("Hello")

@Serializable
object UniverseTestRoute : AppRoute("UniverseTest")

@Serializable
object ExampleListRoute : AppRoute("Examples")

@Serializable
object AppSettingsRoute : AppRoute("Settings")

@Serializable
object StarFeedRoute : AppRoute("StarFeed")

@Serializable
object GalaxyFeedRoute : AppRoute("GalaxyFeed")

@Serializable
object QuestionFeedRoute : AppRoute("QuestionFeed")

@Serializable
data class QuestionProfileRoute(val questionId: String) : IdRoute<String>("Question", questionId) {
    companion object {
        const val TITLE = "Question"
        fun matchRoute(path: String) = matchStringIdRoute(path, TITLE) { QuestionProfileRoute(it) }
    }
}

@Serializable
data class StarProfileRoute(val starId: String) : IdRoute<String>("Star", starId) {
    companion object {
        const val TITLE = "Star"
        fun matchRoute(path: String) = matchStringIdRoute(path, TITLE) { StarProfileRoute(it) }
    }
}

@Serializable
data class GalaxyProfileRoute(val galaxyId: String) : IdRoute<String>("Galaxy", galaxyId) {
    companion object {
        const val TITLE = "Galaxy"
        fun matchRoute(path: String) = matchStringIdRoute(path, TITLE) { GalaxyProfileRoute(it) }
    }
}

@Serializable
data class ExampleProfileRoute(val exampleId: Long) : AppRoute(TITLE) {
    companion object {
        const val TITLE = "Example"
        fun matchRoute(path: String) = matchLongIdRoute(path, TITLE) { ExampleProfileRoute(it) }
    }
}