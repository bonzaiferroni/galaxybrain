package ponder.galaxy.app.io

import ponder.galaxy.model.Api
import ponder.galaxy.model.data.NewQuestion
import ponder.galaxy.model.data.Question
import ponder.galaxy.model.data.QuestionId
import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient

// Ahoy! Rustbeard here, chartin' the currents o' the Question API.
class QuestionApiClient(
    private val client: NeoApiClient = globalNeoApiClient
) {
    suspend fun readAll() = client.request(Api.Questions.All)

    suspend fun readById(questionId: QuestionId) = client.getById(Api.Questions, questionId)

    suspend fun create(question: NewQuestion) = client.request(Api.Questions.Create, question)
}
