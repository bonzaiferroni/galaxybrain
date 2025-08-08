package ponder.galaxy.io

import ponder.galaxy.model.Api
import ponder.galaxy.model.data.GalaxyId
import pondui.io.ApiClient
import pondui.io.globalApiClient

class GalaxySource(
    private val client: ApiClient = globalApiClient
) {
    suspend fun readAll() = client.get(Api.Galaxies.All)

    suspend fun readGalaxyById(galaxyId: GalaxyId) = client.get(Api.Galaxies, galaxyId)
}