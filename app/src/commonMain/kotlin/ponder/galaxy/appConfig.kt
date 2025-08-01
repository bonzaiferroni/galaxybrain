package ponder.galaxy

import compose.icons.TablerIcons
import compose.icons.tablericons.BrandReddit
import compose.icons.tablericons.Heart
import compose.icons.tablericons.Home
import compose.icons.tablericons.Rocket
import compose.icons.tablericons.Settings
import compose.icons.tablericons.YinYang
import kotlinx.collections.immutable.persistentListOf
import ponder.galaxy.ui.AppSettingsScreen
import ponder.galaxy.ui.ExampleListScreen
import ponder.galaxy.ui.ExampleProfileScreen
import ponder.galaxy.ui.HelloScreen
import ponder.galaxy.ui.RedditFeedScreen
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
        RouteConfig(AppSettingsRoute::matchRoute) { defaultScreen<AppSettingsRoute> { AppSettingsScreen() } },
        RouteConfig(RedditFeedRoute::matchRoute) { defaultScreen<RedditFeedRoute> { RedditFeedScreen() } }
    ),
    doors = persistentListOf(
        PortalDoor(TablerIcons.Home, StartRoute),
        PortalDoor(TablerIcons.BrandReddit, RedditFeedRoute),
        PortalDoor(TablerIcons.Settings, AppSettingsRoute)
    ),
)