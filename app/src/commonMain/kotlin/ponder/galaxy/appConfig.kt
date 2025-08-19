package ponder.galaxy

import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.AccessPoint
import compose.icons.tablericons.BrandReddit
import compose.icons.tablericons.Heart
import compose.icons.tablericons.Home
import compose.icons.tablericons.Settings
import compose.icons.tablericons.Stars
import kotlinx.collections.immutable.persistentListOf
import ponder.galaxy.ui.AppConfigScreen
import ponder.galaxy.ui.GalaxyFeedScreen
import ponder.galaxy.ui.GalaxyProfileScreen
import ponder.galaxy.ui.StarFeedScreen
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
        RouteConfig(StarFeedRoute::matchRoute) { defaultScreen<StarFeedRoute>(0.dp) { StarFeedScreen() } },
        RouteConfig(StarProfileRoute::matchRoute) { defaultScreen<StarProfileRoute>(0.dp) { StarProfileScreen(it) } },
        RouteConfig(GalaxyFeedRoute::matchRoute) { defaultScreen<GalaxyFeedRoute> { GalaxyFeedScreen() } },
        RouteConfig(GalaxyProfileRoute::matchRoute) { defaultScreen<GalaxyProfileRoute> { GalaxyProfileScreen(it) } },
    ),
    doors = persistentListOf(
        PortalDoor(TablerIcons.Home, StartRoute),
        PortalDoor(TablerIcons.BrandReddit, StarFeedRoute),
        PortalDoor(TablerIcons.AccessPoint, GalaxyFeedRoute),
        PortalDoor(TablerIcons.Settings, AppSettingsRoute),
    ),
)