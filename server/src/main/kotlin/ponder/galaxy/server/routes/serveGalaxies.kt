package ponder.galaxy.server.routes

import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import ponder.galaxy.model.Api
import ponder.galaxy.server.db.services.GalaxyTableDao
import klutch.server.get

fun Routing.serveGalaxies(
    service: GalaxyTableDao = GalaxyTableDao()
) {
    get(Api.Galaxies.All) { endpoint ->
        service.readAll()
    }
}