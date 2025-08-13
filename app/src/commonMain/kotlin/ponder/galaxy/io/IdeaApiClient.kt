package ponder.galaxy.io

import kotlinx.datetime.Instant
import ponder.galaxy.model.Api
import ponder.galaxy.model.data.StarId
import pondui.io.ApiClient
import pondui.io.globalApiClient

class IdeaApiClient(
    private val client: ApiClient = globalApiClient
) {
    suspend fun readIdeas(since: Instant) = client.get(Api.Ideas, Api.Ideas.since.write(since))

    suspend fun readIdeasByStarId(starId: StarId) = client.get(Api.Ideas.ByStar, starId)
}