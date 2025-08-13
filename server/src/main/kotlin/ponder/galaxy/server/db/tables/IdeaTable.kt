package ponder.galaxy.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ponder.galaxy.model.data.Idea
import ponder.galaxy.model.data.IdeaId
import ponder.galaxy.model.data.StarId

internal object IdeaTable: UUIDTable("idea") {
    val audioUrl = text("audio_url").nullable()
    val textContent = text("text").nullable()
    val imageUrl = text("image_url").nullable()
    val thumbUrl = text("thumb_url").nullable()
    val createdAt = datetime("created_at")
}

internal object StarIdeaTable: Table("star_idea") {
    val starId = reference("star_id", StarTable, onDelete = ReferenceOption.CASCADE)
    val ideaId = reference("idea_id", IdeaTable, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(columns = arrayOf(starId, ideaId))

    init {
        index(false, starId)
        index(false, ideaId)
    }
}

internal fun ResultRow.toIdea() = Idea(
    ideaId = IdeaId(this[IdeaTable.id].value.toStringId()),
    audioUrl = this[IdeaTable.audioUrl],
    text = this[IdeaTable.textContent],
    imageUrl = this[IdeaTable.imageUrl],
    thumbUrl = this[IdeaTable.thumbUrl],
    createdAt = this[IdeaTable.createdAt].toInstantFromUtc()
)

internal fun UpdateBuilder<*>.writeFull(idea: Idea) {
    this[IdeaTable.id] = idea.ideaId.toUUID()
    writeUpdate(idea)
}

internal fun UpdateBuilder<*>.writeUpdate(idea: Idea) {
    this[IdeaTable.audioUrl] = idea.audioUrl
    this[IdeaTable.textContent] = idea.text
    this[IdeaTable.imageUrl] = idea.imageUrl
    this[IdeaTable.thumbUrl] = idea.thumbUrl
    this[IdeaTable.createdAt] = idea.createdAt.toLocalDateTimeUtc()
}
