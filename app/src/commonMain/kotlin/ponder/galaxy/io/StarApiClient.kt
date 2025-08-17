package ponder.galaxy.io

import ponder.galaxy.model.Api
import ponder.galaxy.model.data.StarId
import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient

class StarApiClient(
    private val client: NeoApiClient = globalNeoApiClient
) {
    suspend fun readStarLogs(starIds: List<StarId>) = client.request(Api.StarLogs.Multi, starIds)
    suspend fun readStars(starIds: List<StarId>) = client.request(Api.Stars.Multi,starIds)
}