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
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLink
import ponder.galaxy.model.data.StarLinkId

internal object StarLinkTable : UUIDTable("star_link") {
    val fromStarId = reference("from_star_id", StarTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val toStarId = reference("to_star_id", StarTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val url = text("url")
    val createdAt = datetime("created_at")
}

internal fun ResultRow.toStarLink() = StarLink(
    starLinkId = StarLinkId(this[StarLinkTable.id].value.toStringId()),
    fromStarId = this[StarLinkTable.fromStarId]?.value?.toStringId()?.let(::StarId),
    toStarId = this[StarLinkTable.toStarId]?.value?.toStringId()?.let(::StarId),
    url = this[StarLinkTable.url],
    createdAt = this[StarLinkTable.createdAt].toInstantFromUtc(),
)

internal fun UpdateBuilder<*>.writeFull(starLink: StarLink) {
    this[StarLinkTable.id] = starLink.starLinkId.toUUID()
    this[StarLinkTable.fromStarId] = starLink.fromStarId?.toUUID()
    this[StarLinkTable.toStarId] = starLink.toStarId?.toUUID()
    this[StarLinkTable.createdAt] = starLink.createdAt.toLocalDateTimeUtc()
    writeUpdate(starLink)
}

internal fun UpdateBuilder<*>.writeUpdate(starLink: StarLink) {
    this[StarLinkTable.url] = starLink.url
}
