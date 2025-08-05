package ponder.galaxy.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLog
import ponder.galaxy.model.data.StarLogId

object StarLogTable: LongIdTable("star_log") {
    val starId = reference("star_id", StarTable, onDelete = ReferenceOption.CASCADE)
    val visibility = float("visibility")
    val rise = float("rise")
    val commentCount = integer("comment_count")
    val voteCount = integer("vote_count")
    val createdAt = datetime("created_at")
}

internal fun ResultRow.toStarLog() = StarLog(
    starLogId = StarLogId(this[StarLogTable.id].value),
    starId = StarId(this[StarLogTable.starId].value.toStringId()),
    visibility = this[StarLogTable.visibility],
    rise = this[StarLogTable.rise],
    commentCount = this[StarLogTable.commentCount],
    voteCount = this[StarLogTable.voteCount],
    createdAt = this[StarLogTable.createdAt].toInstantFromUtc()
)

internal fun UpdateBuilder<*>.writeFull(starLog: StarLog) {
    this[StarLogTable.starId] = starLog.starId.toUUID()
    this[StarLogTable.visibility] = starLog.visibility
    this[StarLogTable.rise] = starLog.rise
    this[StarLogTable.commentCount] = starLog.commentCount
    this[StarLogTable.voteCount] = starLog.voteCount
    this[StarLogTable.createdAt] = starLog.createdAt.toLocalDateTimeUtc()
}
