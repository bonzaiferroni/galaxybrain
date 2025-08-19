package ponder.galaxy.io

import ponder.galaxy.model.Api
import ponder.galaxy.model.data.GalaxyId
import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient

class GalaxyApiClient(
    private val client: NeoApiClient = globalNeoApiClient
) {
    suspend fun readAll() = client.request(Api.Galaxies.All)

    suspend fun readGalaxyById(galaxyId: GalaxyId) = client.getById(Api.Galaxies, galaxyId)
}
