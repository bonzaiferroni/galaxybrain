@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.vector
import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ponder.galaxy.model.data.QuestionId
import ponder.galaxy.model.data.Universe
import ponder.galaxy.model.data.UniverseId
import kotlin.uuid.ExperimentalUuidApi

internal object UniverseTable : UUIDTable("universe") {
    val questionId = reference("question_id", QuestionTable, onDelete = ReferenceOption.CASCADE).index()
    val label = text("label")
    val definition = text("definition")
    val imgUrl = text("img_url").nullable()
    val thumbUrl = text("thumb_url").nullable()
    val interval = integer("interval_minutes")
    val coherence = float("coherence").nullable()
    val signal = float("signal").nullable()
    val embedding = vector("vector", size = 768)
    val createdAt = datetime("created_at")
}

internal fun ResultRow.toUniverse() = Universe(
    questionId = QuestionId(this[UniverseTable.questionId].value.toStringId()),
    universeId = UniverseId(this[UniverseTable.id].value.toStringId()),
    label = this[UniverseTable.label],
    definition = this[UniverseTable.definition],
    imgUrl = this[UniverseTable.imgUrl],
    thumbUrl = this[UniverseTable.thumbUrl],
    interval = this[UniverseTable.interval],
    coherence = this[UniverseTable.coherence],
    signal = this[UniverseTable.signal],
    embedding = this[UniverseTable.embedding],
    createdAt = this[UniverseTable.createdAt].toInstantFromUtc(),
)

internal fun UpdateBuilder<*>.writeFull(universe: Universe) {
    this[UniverseTable.id] = universe.universeId.value.toUUID()
    this[UniverseTable.questionId] = universe.questionId.value.toUUID()
    this[UniverseTable.createdAt] = universe.createdAt.toLocalDateTimeUtc()
    writeUpdate(universe)
}

internal fun UpdateBuilder<*>.writeUpdate(universe: Universe) {
    this[UniverseTable.label] = universe.label
    this[UniverseTable.definition] = universe.definition
    this[UniverseTable.imgUrl] = universe.imgUrl
    this[UniverseTable.thumbUrl] = universe.thumbUrl
    this[UniverseTable.interval] = universe.interval
    this[UniverseTable.coherence] = universe.coherence
    this[UniverseTable.signal] = universe.signal
    this[UniverseTable.embedding] = universe.embedding
}
