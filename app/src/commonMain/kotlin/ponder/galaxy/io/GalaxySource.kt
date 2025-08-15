package ponder.galaxy.io

import ponder.galaxy.model.Api
import ponder.galaxy.model.data.GalaxyId
import pondui.io.NeoApiClient
import pondui.io.globalApiClient
import pondui.io.globalNeoApiClient

class GalaxySource(
    private val client: NeoApiClient = globalNeoApiClient
) {
    suspend fun readAll() = client.request(Api.Galaxies.All)

    suspend fun readGalaxyById(galaxyId: GalaxyId) = client.getById(Api.Galaxies, galaxyId)
}
