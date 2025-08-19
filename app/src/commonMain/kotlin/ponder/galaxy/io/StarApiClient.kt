package ponder.galaxy.io

import kabinet.api.write
import ponder.galaxy.model.Api
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.NewStarContent
import ponder.galaxy.model.data.StarId
import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient

class StarApiClient(
    private val client: NeoApiClient = globalNeoApiClient
) {
    suspend fun readStarLogs(starIds: List<StarId>) = client.request(Api.StarLogs.Multi, starIds)

    suspend fun readStars(starIds: List<StarId>) = client.request(Api.Stars.Multi,starIds)

    suspend fun readByUrl(url: String, create: Boolean? = null) = client.request(Api.Stars.ByUrl, url) {
        write(it.create, create)
    }

    suspend fun readById(starId: StarId) = client.getById(Api.Stars, starId)

    suspend fun readLatestByGalaxyId(galaxyId: GalaxyId) = client.getById(Api.Stars.Latest, galaxyId)

    suspend fun updateFromNewContent(newContent: NewStarContent) = client.request(Api.Stars.NewContent, newContent)
}