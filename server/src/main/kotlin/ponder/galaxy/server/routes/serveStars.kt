package ponder.galaxy.server.routes

import io.ktor.server.routing.Routing
import klutch.server.get
import klutch.server.post
import ponder.galaxy.model.Api
import ponder.galaxy.model.data.StarId
import ponder.galaxy.server.db.services.StarLinkTableDao
import ponder.galaxy.server.db.services.StarTableDao

fun Routing.serveStars(
    dao: StarTableDao = StarTableDao(),

) {
    post(Api.Stars.Multi) { starIds, endpoint ->
        dao.readByIds(starIds)
    }

    get(Api.Stars, { StarId(it) }) { starId, endpoint ->
        dao.readByIdOrNull(starId)
    }
}