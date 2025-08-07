package ponder.galaxy.server.routes

import io.ktor.server.routing.Routing
import klutch.server.get
import ponder.galaxy.model.Api
import ponder.galaxy.server.db.services.StarLogTableDao
import klutch.server.post
import ponder.galaxy.model.data.StarId

fun Routing.serveStarLogs(
    service: StarLogTableDao = StarLogTableDao()
) {
    post(Api.StarLogs.Multi) { starLogIds, endpoint ->
        service.readAllByStarIds(starLogIds)
    }

    get(Api.StarLogs, { StarId(it) }) { starId, endpoint ->
        service.readAllByStarId(starId)
    }
}