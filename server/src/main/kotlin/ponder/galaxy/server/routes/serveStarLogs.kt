package ponder.galaxy.server.routes

import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import ponder.galaxy.model.Api
import ponder.galaxy.server.db.services.StarLogTableDao
import klutch.server.post

fun Routing.serveStarLogs(
    service: StarLogTableDao = StarLogTableDao()
) {
    post(Api.StarLogs) { starLogIds, endpoint ->
        service.readLogsByStarIds(starLogIds)
    }
}