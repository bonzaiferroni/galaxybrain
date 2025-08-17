@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.utils.toStringId
import klutch.utils.toUUID
import klutch.utils.toUuid
import kotlin.uuid.ExperimentalUuidApi
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ponder.galaxy.model.data.CommentId
import ponder.galaxy.model.data.SnippetId
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarSnippet
import ponder.galaxy.model.data.StarSnippetId

internal object StarSnippetTable : UUIDTable("star_snippet") {
    val starId = reference("star_id", StarTable, onDelete = ReferenceOption.CASCADE)
    val snippetId = reference("snippet_id", SnippetTable, onDelete = ReferenceOption.CASCADE)
    val commentId = reference("comment_id", CommentTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val index = integer("text_index")
    val createdAt = datetime("created_at")
}

internal fun ResultRow.toStarSnippet() = StarSnippet(
    starSnippetId = StarSnippetId(this[StarSnippetTable.id].value.toUuid()),
    snippetId = SnippetId(this[StarSnippetTable.snippetId].value.toUuid()),
    starId = StarId(this[StarSnippetTable.starId].value.toStringId()),
    commentId = this[StarSnippetTable.commentId]?.value?.toStringId()?.let(::CommentId),
    index = this[StarSnippetTable.index],
    createdAt = this[StarSnippetTable.createdAt].toInstantFromUtc(),
)

internal fun UpdateBuilder<*>.writeFull(starSnippet: StarSnippet) {
    this[StarSnippetTable.id] = starSnippet.starSnippetId.value.toUUID()
    this[StarSnippetTable.snippetId] = starSnippet.snippetId.value.toUUID()
    this[StarSnippetTable.starId] = starSnippet.starId.toUUID()
    this[StarSnippetTable.commentId] = starSnippet.commentId?.toUUID()
    this[StarSnippetTable.createdAt] = starSnippet.createdAt.toLocalDateTimeUtc()
    writeUpdate(starSnippet)
}

internal fun UpdateBuilder<*>.writeUpdate(starSnippet: StarSnippet) {
    this[StarSnippetTable.index] = starSnippet.index
}
