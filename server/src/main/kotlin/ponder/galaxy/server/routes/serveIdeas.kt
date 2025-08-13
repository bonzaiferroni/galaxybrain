package ponder.galaxy.server.routes

import io.ktor.server.routing.Routing
import klutch.server.get
import klutch.server.readParamOrNull
import ponder.galaxy.model.Api
import ponder.galaxy.model.data.StarId
import ponder.galaxy.server.db.services.IdeaService
import ponder.galaxy.server.db.services.IdeaTableDao

fun Routing.serveIdeas(
    service: IdeaService = IdeaService(),
    dao: IdeaTableDao = IdeaTableDao()
) {
    // GET a single Idea by StarId, creating it if missing
    get(Api.Ideas.ByStar, { StarId(it) }) { starId, endpoint ->
        service.readOrCreateByStarId(starId)
    }

    get(Api.Ideas) { endpoint ->
        val since = Api.Ideas.since.readParamOrNull(call)
        dao.readIdeas(since)
    }
}
