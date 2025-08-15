package ponder.galaxy.server.routes

import io.ktor.server.routing.Routing
import klutch.server.get
import ponder.galaxy.model.Api
import ponder.galaxy.model.data.StarId
import ponder.galaxy.server.db.services.StarLinkTableDao

fun Routing.serveStarLinks(
    dao: StarLinkTableDao = StarLinkTableDao(),
) {
    get(Api.StarLinks.Outgoing, { StarId(it)}) { starId, _ ->
        dao.readOutgoingByStarId(starId)
    }
}