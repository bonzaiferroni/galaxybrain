package ponder.galaxy.server.routes

import io.ktor.server.routing.Routing
import klutch.server.get
import klutch.server.readParamOrNull
import ponder.galaxy.model.Api
import ponder.galaxy.model.data.CommentId
import ponder.galaxy.model.data.StarId
import ponder.galaxy.server.db.services.IDEA_CONTENT_DESCRIPTION
import ponder.galaxy.server.db.services.IDEA_HEADLINE_DESCRIPTION
import ponder.galaxy.server.db.services.IdeaService
import ponder.galaxy.server.db.services.IdeaTableDao

fun Routing.serveIdeas(
    service: IdeaService = IdeaService(),
) {
    get(Api.Ideas.Headline, { StarId(it) }) { starId, endpoint ->
        val create = endpoint.create.readParamOrNull(call)
        service.dao.readIdeas(starId, IDEA_HEADLINE_DESCRIPTION).firstOrNull() ?: when (create) {
            true -> service.createFromHeadline(starId)
            else -> null
        }
    }

    get(Api.Ideas.Content, { StarId(it)}) { starId, endpoint ->
        val create = endpoint.create.readParamOrNull(call)
        service.dao.readIdeas(starId, IDEA_CONTENT_DESCRIPTION).firstOrNull() ?: when (create) {
            true -> service.createFromContent(starId)
            else -> null
        }
    }

    get(Api.Ideas.Comment, { CommentId(it)}) { commentId, endpoint ->
        val create = endpoint.create.readParamOrNull(call)
        service.dao.readIdeas(commentId).firstOrNull() ?: when (create) {
            true -> service.createFromComment(commentId)
            else -> null
        }
    }

    get(Api.Ideas) { endpoint ->
        val since = endpoint.since.readParamOrNull(call)
        service.dao.readIdeas(since)
    }
}
