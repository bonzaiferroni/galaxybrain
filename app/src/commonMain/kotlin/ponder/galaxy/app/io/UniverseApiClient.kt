package ponder.galaxy.app.io

import ponder.galaxy.model.Api
import ponder.galaxy.model.data.NewUniverse
import ponder.galaxy.model.data.QuestionId
import ponder.galaxy.model.data.Universe
import ponder.galaxy.model.data.UniverseId
import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient

// Avast! Rustbeard chartin' the starry seas o' the Universe API, arrr!
class UniverseApiClient(
    private val client: NeoApiClient = globalNeoApiClient
) {
    suspend fun readById(universeId: UniverseId): Universe? = client.getById(Api.Universes, universeId)

    suspend fun readByQuestion(questionId: QuestionId): List<Universe>? = client.getById(Api.Universes.ByQuestion, questionId)

    suspend fun create(universe: NewUniverse) = client.request(Api.Universes.Create, universe)
}
