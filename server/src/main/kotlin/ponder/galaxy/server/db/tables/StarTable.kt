package ponder.galaxy.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId

internal object StarTable: UUIDTable("star") {
    val galaxyId = reference("galaxy_id", GalaxyTable, onDelete = ReferenceOption.CASCADE)
    val identifier = text("identifier")
    val title = text("title")
    val textContent = text("text_content").nullable()
    val link = text("link")
    val permalink = text("permalink")
    val thumbUrl = text("thumb_url").nullable()
    val imageUrl = text("image_url").nullable()
    val visibility = float("visibility")
    val commentCount = integer("comment_count")
    val voteCount = integer("vote_count")
    val updatedAt = datetime("updated_at")
    val createdAt = datetime("created_at")
    val accessedAt = datetime("accessed_at")
}

internal fun ResultRow.toStar() = Star(
    starId = StarId(this[StarTable.id].value.toStringId()),
    galaxyId = GalaxyId(this[StarTable.galaxyId].value.toStringId()),
    identifier = this[StarTable.identifier],
    title = this[StarTable.title],
    textContent = this[StarTable.textContent],
    link = this[StarTable.link],
    permalink = this[StarTable.permalink],
    thumbUrl = this[StarTable.thumbUrl],
    imageUrl = this[StarTable.imageUrl],
    visibility = this[StarTable.visibility],
    commentCount = this[StarTable.commentCount],
    voteCount = this[StarTable.voteCount],
    updatedAt = this[StarTable.updatedAt].toInstantFromUtc(),
    createdAt = this[StarTable.createdAt].toInstantFromUtc(),
    accessedAt = this[StarTable.accessedAt].toInstantFromUtc()
)

internal fun UpdateBuilder<*>.writeFull(star: Star) {
    this[StarTable.id] = star.starId.toUUID()
    this[StarTable.galaxyId] = star.galaxyId.toUUID()
    this[StarTable.identifier] = star.identifier
    this[StarTable.createdAt] = star.createdAt.toLocalDateTimeUtc()
    this[StarTable.accessedAt] = star.accessedAt.toLocalDateTimeUtc()
    writeUpdate(star)
}

internal fun UpdateBuilder<*>.writeUpdate(star: Star) {
    this[StarTable.title] = star.title
    this[StarTable.textContent] = star.textContent
    this[StarTable.link] = star.link
    this[StarTable.permalink] = star.permalink
    this[StarTable.thumbUrl] = star.thumbUrl
    this[StarTable.imageUrl] = star.imageUrl
    this[StarTable.visibility] = star.visibility
    this[StarTable.commentCount] = star.commentCount
    this[StarTable.voteCount] = star.voteCount
    this[StarTable.updatedAt] = star.updatedAt.toLocalDateTimeUtc()
}
