package ponder.galaxy.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import klutch.gemini.serveGemini
import klutch.server.*
import ponder.galaxy.model.Api
import ponder.galaxy.server.routes.*
import java.io.File

fun Application.configureApiRoutes() {
    routing {
        get(Api.path) {
            call.respondText("Hello World!")
        }

        serveUsers()

        staticFiles("img", File("img"))
        staticFiles("wav", File("wav"))

        serveStars()
        serveStarLogs()
        serveGalaxies()
        serveIdeas()
        serveGemini(Api.Gemini)
    }
}