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

    suspend fun readHeadlineIdea(starId: StarId, create: Boolean = false) =
        client.getOrNull(Api.Ideas.Headline, starId, Api.Ideas.Headline.create.write(create))

    suspend fun readContentIdea(starId: StarId, create: Boolean = false) =
        client.getOrNull(Api.Ideas.Content, starId, Api.Ideas.Content.create.write(create))
}