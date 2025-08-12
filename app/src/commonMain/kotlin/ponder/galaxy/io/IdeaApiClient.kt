package ponder.galaxy.io

import ponder.galaxy.model.Api
import ponder.galaxy.model.data.StarId
import pondui.io.ApiClient
import pondui.io.globalApiClient

class IdeaApiClient(
    private val client: ApiClient = globalApiClient
) {
    suspend fun readIdeasByStarId(starId: StarId) = client.get(Api.Ideas.ByStar, starId)
}