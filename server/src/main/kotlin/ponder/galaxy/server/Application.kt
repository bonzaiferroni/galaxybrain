package ponder.galaxy.server

import io.ktor.server.application.*
import klutch.server.configureSecurity
import ponder.galaxy.server.plugins.configureApiRoutes
import ponder.galaxy.server.plugins.configureCors
import ponder.galaxy.server.plugins.configureDatabases
import ponder.galaxy.server.plugins.configureLogging
import ponder.galaxy.server.plugins.configureSerialization
import ponder.galaxy.server.plugins.configureWebSockets

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureCors()
    configureSerialization()
    configureDatabases()
    configureSecurity()
    configureApiRoutes()
    configureWebSockets()
    configureLogging()
}