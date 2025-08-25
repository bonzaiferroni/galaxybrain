package ponder.galaxy.app.io

import ponder.galaxy.model.Api
import ponder.galaxy.model.data.NewUniverse
import ponder.galaxy.model.data.QuestionId
import ponder.galaxy.model.data.Universe
import ponder.galaxy.model.data.UniverseId
import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient

class UniverseApiClient(
    private val client: NeoApiClient = globalNeoApiClient
) {
    suspend fun readById(universeId: UniverseId): Universe? = client.getById(Api.Universes, universeId)

    suspend fun readByQuestion(questionId: QuestionId): List<Universe>? = client.getById(Api.Universes.ByQuestion, questionId)

    suspend fun create(universe: NewUniverse) = client.request(Api.Universes.Create, universe)

    suspend fun readScansByUniverseId(universeId: UniverseId) = client.getById(Api.Universes.Scans, universeId)
}
