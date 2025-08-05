package ponder.galaxy.io

import ponder.galaxy.model.Api
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLogId
import pondui.io.ApiClient
import pondui.io.globalApiClient

class StarSource(
    private val client: ApiClient = globalApiClient
) {
    suspend fun readStarLogs(starIds: List<StarId>) = client.post(Api.StarLogs, starIds)
    suspend fun readStars(starIds: List<StarId>) = client.post(Api.Stars.Multi,starIds)
}