package ponder.galaxy.server.db.services

import kabinet.console.globalConsole
import klutch.db.DbService
import kotlinx.datetime.Clock
import ponder.galaxy.model.data.NewQuestion
import ponder.galaxy.model.data.Question
import ponder.galaxy.model.data.QuestionId

private val console = globalConsole.getHandle(QuestionTableService::class)

class QuestionTableService(val dao: QuestionTableDao = QuestionTableDao()) : DbService() {
    suspend fun createQuestion(question: NewQuestion): Boolean = dbQuery {
        dao.insert(Question(
            questionId = QuestionId.random(),
            text = question.text,
            createdAt = Clock.System.now()
        ))
        console.log("Created question: ${question.text}")
        true
    }
}
