package ponder.galaxy

import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.BrandReddit
import compose.icons.tablericons.Heart
import compose.icons.tablericons.Home
import compose.icons.tablericons.Settings
import kotlinx.collections.immutable.persistentListOf
import ponder.galaxy.ui.AppConfigScreen
import ponder.galaxy.ui.GalaxyFeedScreen
import ponder.galaxy.ui.StarProfileScreen
import ponder.galaxy.ui.StartScreen
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
        RouteConfig(GalaxyFeedRoute::matchRoute) { defaultScreen<GalaxyFeedRoute>(0.dp) { GalaxyFeedScreen() } },
        RouteConfig(StarProfileRoute::matchRoute) { defaultScreen<StarProfileRoute>(0.dp) { StarProfileScreen(it) } }
    ),
    doors = persistentListOf(
        PortalDoor(TablerIcons.Home, StartRoute),
        PortalDoor(TablerIcons.BrandReddit, GalaxyFeedRoute),
        PortalDoor(TablerIcons.Settings, AppSettingsRoute),
    ),
)