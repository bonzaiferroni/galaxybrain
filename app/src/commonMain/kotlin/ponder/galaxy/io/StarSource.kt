package ponder.galaxy.io

import ponder.galaxy.model.Api
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLogId
import pondui.io.NeoApiClient
import pondui.io.globalApiClient
import pondui.io.globalNeoApiClient

class StarSource(
    private val client: NeoApiClient = globalNeoApiClient
) {
    suspend fun readStarLogs(starIds: List<StarId>) = client.request(Api.StarLogs.Multi, starIds)
    suspend fun readStars(starIds: List<StarId>) = client.request(Api.Stars.Multi,starIds)
}