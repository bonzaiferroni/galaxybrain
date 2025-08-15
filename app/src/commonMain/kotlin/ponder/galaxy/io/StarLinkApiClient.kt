package ponder.galaxy.io

import ponder.galaxy.model.Api
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLink
import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient

class StarLinkApiClient(
    private val client: NeoApiClient = globalNeoApiClient
) {
    suspend fun readOutgoingLinks(starId: StarId): List<StarLink>? =
        client.getById(Api.StarLinks.Outgoing, starId)
}
