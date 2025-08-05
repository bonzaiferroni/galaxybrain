package ponder.galaxy.server.db.tables

import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.BaseBatchInsertStatement
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.statements.UpdateStatement
import ponder.galaxy.model.data.Galaxy
import ponder.galaxy.model.data.GalaxyId
import java.util.UUID

internal object GalaxyTable: UUIDTable("galaxy") {
    val name = text("name")
    val url = text("url")
    val visibility = float("visibility")
}

internal fun ResultRow.toGalaxy() = Galaxy(
    galaxyId = GalaxyId(this[GalaxyTable.id].value.toStringId()),
    name = this[GalaxyTable.name],
    url = this[GalaxyTable.url],
    visibility = this[GalaxyTable.visibility]
)

internal fun UpdateBuilder<*>.writeFull(galaxy: Galaxy) {
    this[GalaxyTable.id] = galaxy.galaxyId.toUUID()
    writeUpdate(galaxy)
}

internal fun UpdateBuilder<*>.writeUpdate(galaxy: Galaxy) {
    this[GalaxyTable.name] = galaxy.name
    this[GalaxyTable.url] = galaxy.url
    this[GalaxyTable.visibility] = galaxy.visibility
}