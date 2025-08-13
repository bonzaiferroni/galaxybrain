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
    get(Api.Ideas.Headline, { StarId(it) }) { starId, _ ->
        service.readOrCreateFromHeadline(starId)
    }

    get(Api.Ideas.Content, { StarId(it)}) { starId, _ ->
        service.readOrCreateFromContent(starId)
    }

    get(Api.Ideas) { _ ->
        val since = Api.Ideas.since.readParamOrNull(call)
        dao.readIdeas(since)
    }
}
