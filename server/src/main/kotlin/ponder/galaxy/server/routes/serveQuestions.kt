package ponder.galaxy.server.routes

import io.ktor.server.routing.Routing
import klutch.server.get
import klutch.server.post
import ponder.galaxy.model.Api
import ponder.galaxy.model.data.Question
import ponder.galaxy.model.data.QuestionId
import ponder.galaxy.server.db.services.QuestionTableService
import kotlin.uuid.ExperimentalUuidApi

fun Routing.serveQuestions(
    service: QuestionTableService = QuestionTableService(),
) {
    get(Api.Questions, { QuestionId(it) }) { questionId, endpoint ->
        service.dao.readByIdOrNull(questionId)
    }

    get(Api.Questions.All) { endpoint ->
        service.dao.readAll()
    }

    post(Api.Questions.Create) { question, endpoint ->
        service.createQuestion(question)
    }
}
