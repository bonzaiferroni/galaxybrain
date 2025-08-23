@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.utils.toStringId
import klutch.utils.toUUID
import klutch.utils.toUuid
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ponder.galaxy.model.data.Comment
import ponder.galaxy.model.data.CommentId
import ponder.galaxy.model.data.StarId
import kotlin.uuid.ExperimentalUuidApi

internal object CommentTable : UUIDTable("comment") {
    val parentId = reference("parent_id", CommentTable, onDelete = ReferenceOption.CASCADE).nullable().index()
    val starId = reference("star_id", StarTable, onDelete = ReferenceOption.CASCADE).index()
    val identifier = text("identifier")
    val author = text("author")
    val depth = integer("depth").nullable()
    val visibility = float("visibility")
    val visibilityRatio = float("visibility_ratio")
    val voteCount = integer("vote_count")
    val replyCount = integer("reply_count")
    val permalink = text("permalink")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
    val accessedAt = datetime("accessed_at")
}

internal fun ResultRow.toComment() = Comment(
    commentId = CommentId(this[CommentTable.id].value.toStringId()),
    parentId = this[CommentTable.parentId]?.value?.toStringId()?.let(::CommentId),
    starId = StarId(this[CommentTable.starId].value.toStringId()),
    identifier = this[CommentTable.identifier],
    author = this[CommentTable.author],
    depth = this[CommentTable.depth],
    visibility = this[CommentTable.visibility],
    visibilityRatio = this[CommentTable.visibilityRatio],
    voteCount = this[CommentTable.voteCount],
    replyCount = this[CommentTable.replyCount],
    permalink = this[CommentTable.permalink],
    createdAt = this[CommentTable.createdAt].toInstantFromUtc(),
    updatedAt = this[CommentTable.updatedAt].toInstantFromUtc(),
    accessedAt = this[CommentTable.accessedAt].toInstantFromUtc()
)

internal fun UpdateBuilder<*>.writeFull(comment: Comment) {
    this[CommentTable.id] = comment.commentId.toUUID()
    this[CommentTable.parentId] = comment.parentId?.toUUID()
    this[CommentTable.starId] = comment.starId.toUUID()
    this[CommentTable.identifier] = comment.identifier
    this[CommentTable.createdAt] = comment.createdAt.toLocalDateTimeUtc()
    this[CommentTable.accessedAt] = comment.accessedAt.toLocalDateTimeUtc()
    this[CommentTable.author] = comment.author
    this[CommentTable.depth] = comment.depth
    this[CommentTable.permalink] = comment.permalink
    writeUpdate(comment)
}

internal fun UpdateBuilder<*>.writeUpdate(comment: Comment) {
    this[CommentTable.visibility] = comment.visibility
    this[CommentTable.visibilityRatio] = comment.visibilityRatio
    this[CommentTable.voteCount] = comment.voteCount
    this[CommentTable.replyCount] = comment.replyCount
    this[CommentTable.updatedAt] = comment.updatedAt.toLocalDateTimeUtc()
}
