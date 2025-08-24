@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.services

import klutch.db.DbService
import klutch.db.batchUpdate
import klutch.db.read
import klutch.db.readByIdOrNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import ponder.galaxy.model.data.Question
import ponder.galaxy.model.data.QuestionId
import ponder.galaxy.server.db.tables.QuestionTable
import ponder.galaxy.server.db.tables.toQuestion
import ponder.galaxy.server.db.tables.writeFull
import ponder.galaxy.server.db.tables.writeUpdate
import klutch.utils.toUUID
import kotlin.uuid.ExperimentalUuidApi

class QuestionTableDao : DbService() {

    suspend fun insert(vararg questions: Question) = dbQuery {
        QuestionTable.batchInsert(questions.toList()) { writeFull(it) }
    }

    suspend fun upsert(vararg questions: Question) = dbQuery {
        QuestionTable.batchUpsert(questions.toList()) { writeFull(it) }
    }

    suspend fun update(vararg questions: Question) = dbQuery {
        QuestionTable.batchUpdate(questions.toList(), { it.questionId.toUUID() }) { writeUpdate(it) }
    }

    suspend fun delete(vararg questions: Question) = dbQuery {
        QuestionTable.deleteWhere { QuestionTable.id inList questions.map { it.questionId.toUUID() } }
    }

    suspend fun readByIdOrNull(questionId: QuestionId) = dbQuery {
        QuestionTable.readByIdOrNull(questionId.toUUID())?.toQuestion()
    }

    suspend fun readByIds(questionIds: List<QuestionId>) = dbQuery {
        QuestionTable.read { it.id.inList(questionIds.map { id -> id.toUUID() }) }.map { it.toQuestion() }
    }

    suspend fun readAll() = dbQuery {
        QuestionTable.select(QuestionTable.columns).map { it.toQuestion() }
    }
}
