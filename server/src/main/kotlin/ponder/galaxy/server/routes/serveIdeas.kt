package ponder.galaxy.server.routes

import io.ktor.server.routing.Routing
import klutch.server.get
import ponder.galaxy.model.Api
import ponder.galaxy.model.data.StarId
import ponder.galaxy.server.db.services.IdeaService

fun Routing.serveIdeas(
    service: IdeaService = IdeaService(),
) {
    // GET a single Idea by StarId, creating it if missing
    get(Api.Ideas.ByStar, { StarId(it) }) { starId, endpoint ->
        service.readOrCreateByStarId(starId)
    }
}
