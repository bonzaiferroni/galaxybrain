@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.routes

import io.ktor.server.routing.Routing
import klutch.server.get
import klutch.server.post
import ponder.galaxy.model.Api
import ponder.galaxy.model.data.SnippetId
import ponder.galaxy.model.data.StarId
import ponder.galaxy.server.db.services.SnippetTableService
import kotlin.uuid.ExperimentalUuidApi

fun Routing.serveSnippets(
    service: SnippetTableService = SnippetTableService(),
) {
    // Returns the snippets associated with a given Star, ordered by their index
    get(Api.Snippets.StarSnippets, { StarId(it) }) { starId, _ ->
        service.dao.readStarSnippets(starId)
    }

    get(Api.Snippets.Audio, { SnippetId(it) }) { snippetId, _ ->
        service.readOrCreateAudio(snippetId)
    }

    post(Api.Snippets.TestUniverse) { universe, endpoint ->
        service.testUniverse(universe)
    }
}
