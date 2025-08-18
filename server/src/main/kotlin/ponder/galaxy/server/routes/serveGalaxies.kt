@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.routes

import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kabinet.utils.toUuid
import ponder.galaxy.model.Api
import ponder.galaxy.server.db.services.GalaxyTableDao
import klutch.server.get
import ponder.galaxy.model.data.GalaxyId
import kotlin.uuid.ExperimentalUuidApi

fun Routing.serveGalaxies(
    service: GalaxyTableDao = GalaxyTableDao()
) {
    get(Api.Galaxies.All) { endpoint ->
        service.readAll()
    }

    get(Api.Galaxies, { GalaxyId(it) }) { id, endpoint ->
        service.readById(id)
    }
}