package ponder.galaxy.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import klutch.server.*
import ponder.galaxy.model.apiPrefix
import ponder.galaxy.server.routes.*

fun Application.configureApiRoutes() {
    routing {
        get(apiPrefix) {
            call.respondText("Hello World!")
        }

        serveUsers()

        serveStars()
        serveStarLogs()
    }
}