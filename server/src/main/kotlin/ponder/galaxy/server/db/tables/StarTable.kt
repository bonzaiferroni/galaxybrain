package ponder.galaxy.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.BaseBatchInsertStatement
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.statements.UpdateStatement
import ponder.galaxy.model.data.Galaxy
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import java.util.UUID

internal object StarTable: UUIDTable("star") {
    val galaxyId = reference("galaxy_id", GalaxyTable, onDelete = ReferenceOption.CASCADE)
    val title = text("title")
    val url = text("url")
    val visibility = float("visibility")
    val updatedAt = datetime("updated_at")
    val createdAt = datetime("created_at")
    val discoveredAt = datetime("discovered_at")
}

internal fun ResultRow.toStar() = Star(
    starId = StarId(this[StarTable.id].value.toStringId()),
    galaxyId = GalaxyId(this[StarTable.galaxyId].value.toStringId()),
    title = this[StarTable.title],
    url = this[StarTable.url],
    visibility = this[StarTable.visibility],
    updatedAt = this[StarTable.updatedAt].toInstantFromUtc(),
    createdAt = this[StarTable.createdAt].toInstantFromUtc(),
    discoveredAt = this[StarTable.discoveredAt].toInstantFromUtc()
)

internal fun UpdateBuilder<*>.write(star: Star) {
    this[StarTable.id] = star.starId.toUUID()
    this[StarTable.galaxyId] = star.galaxyId.toUUID()
    this[StarTable.title] = star.title
    this[StarTable.url] = star.url
    this[StarTable.visibility] = star.visibility
    this[StarTable.updatedAt] = star.updatedAt.toLocalDateTimeUtc()
    this[StarTable.createdAt] = star.createdAt.toLocalDateTimeUtc()
    this[StarTable.discoveredAt] = star.discoveredAt.toLocalDateTimeUtc()
}

internal fun UpdateStatement.update(star: Star) {
    this[StarTable.title] = star.title
    this[StarTable.url] = star.url
    this[StarTable.visibility] = star.visibility
    this[StarTable.updatedAt] = star.updatedAt.toLocalDateTimeUtc()
}
