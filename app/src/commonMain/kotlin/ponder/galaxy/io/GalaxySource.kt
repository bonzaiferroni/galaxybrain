package ponder.galaxy.io

import ponder.galaxy.model.Api
import pondui.io.ApiClient
import pondui.io.globalApiClient

class GalaxySource(
    private val client: ApiClient = globalApiClient
) {
    suspend fun readAll() = client.get(Api.Galaxies.All)
}