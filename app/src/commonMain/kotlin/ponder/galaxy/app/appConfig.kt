package ponder.galaxy.app

import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.AccessPoint
import compose.icons.tablericons.BrandReddit
import compose.icons.tablericons.Heart
import compose.icons.tablericons.Home
import compose.icons.tablericons.QuestionMark
import compose.icons.tablericons.Settings
import compose.icons.tablericons.Space
import kotlinx.collections.immutable.persistentListOf
import ponder.galaxy.app.ui.AppConfigScreen
import ponder.galaxy.app.ui.GalaxyFeedScreen
import ponder.galaxy.app.ui.GalaxyProfileScreen
import ponder.galaxy.app.ui.StarFeedScreen
import ponder.galaxy.app.ui.StarProfileScreen
import ponder.galaxy.app.ui.StartScreen
import ponder.galaxy.app.ui.UniverseTestScreen
import ponder.galaxy.app.ui.QuestionFeedScreen
import ponder.galaxy.app.ui.QuestionProfileScreen
import pondui.ui.core.PondConfig
import pondui.ui.core.RouteConfig
import pondui.ui.nav.PortalDoor
import pondui.ui.nav.defaultScreen

val appConfig = PondConfig(
    name = "galaxybrain",
    logo = TablerIcons.Heart,
    home = StartRoute,
    routes = persistentListOf(
        RouteConfig(StartRoute::matchRoute) { defaultScreen<StartRoute> { StartScreen() } },
        RouteConfig(AppSettingsRoute::matchRoute) { defaultScreen<AppSettingsRoute> { AppConfigScreen() } },
        RouteConfig(StarFeedRoute::matchRoute) { defaultScreen<StarFeedRoute>(0.dp) { StarFeedScreen() } },
        RouteConfig(StarProfileRoute::matchRoute) { defaultScreen<StarProfileRoute>(0.dp) { StarProfileScreen(it) } },
        RouteConfig(GalaxyFeedRoute::matchRoute) { defaultScreen<GalaxyFeedRoute> { GalaxyFeedScreen() } },
        RouteConfig(GalaxyProfileRoute::matchRoute) { defaultScreen<GalaxyProfileRoute> { GalaxyProfileScreen(it) } },
        RouteConfig(UniverseTestRoute::matchRoute) { defaultScreen<UniverseTestRoute> { UniverseTestScreen() } },
        RouteConfig(QuestionFeedRoute::matchRoute) { defaultScreen<QuestionFeedRoute> { QuestionFeedScreen() } },
        RouteConfig(QuestionProfileRoute::matchRoute) { defaultScreen<QuestionProfileRoute> { QuestionProfileScreen(it) } },
    ),
    doors = persistentListOf(
        // PortalDoor(TablerIcons.Home, StartRoute),
        PortalDoor(TablerIcons.BrandReddit, StarFeedRoute),
        PortalDoor(TablerIcons.AccessPoint, GalaxyFeedRoute),
        PortalDoor(TablerIcons.Space, UniverseTestRoute),
        PortalDoor(TablerIcons.QuestionMark, QuestionFeedRoute),
        PortalDoor(TablerIcons.Settings, AppSettingsRoute),
    ),
)