@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.utils.toStringId
import klutch.utils.toUUID
import kotlin.uuid.ExperimentalUuidApi
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ponder.galaxy.model.data.Question
import ponder.galaxy.model.data.QuestionId

internal object QuestionTable : UUIDTable("question") {
    val text = text("text")
    val createdAt = datetime("created_at")
}

internal fun ResultRow.toQuestion() = Question(
    questionId = QuestionId(this[QuestionTable.id].value.toStringId()),
    text = this[QuestionTable.text],
    createdAt = this[QuestionTable.createdAt].toInstantFromUtc(),
)

internal fun UpdateBuilder<*>.writeFull(question: Question) {
    this[QuestionTable.id] = question.questionId.value.toUUID()
    this[QuestionTable.createdAt] = question.createdAt.toLocalDateTimeUtc()
    writeUpdate(question)
}

internal fun UpdateBuilder<*>.writeUpdate(question: Question) {
    this[QuestionTable.text] = question.text
}
