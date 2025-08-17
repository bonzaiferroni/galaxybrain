package ponder.galaxy.io

import ponder.galaxy.model.Api
import ponder.galaxy.model.data.Snippet
import ponder.galaxy.model.data.StarId
import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient

class SnippetApiClient(
    private val client: NeoApiClient = globalNeoApiClient
) {
    suspend fun readByStarId(starId: StarId): List<Snippet>? =
        client.getById(Api.Snippets.Stars, starId)
}
