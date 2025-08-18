@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
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
import ponder.galaxy.model.data.Galaxy
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.HostId

internal object GalaxyTable: UUIDTable("galaxy") {
    val hostId = reference("host_id", HostTable, onDelete = ReferenceOption.CASCADE)
    val name = text("name")
    val url = text("url")
    val visibility = float("visibility")
    val createdAt = datetime("created_at")
}

internal fun ResultRow.toGalaxy() = Galaxy(
    galaxyId = GalaxyId(this[GalaxyTable.id].value.toStringId()),
    hostId = HostId(this[GalaxyTable.hostId].value.toStringId()),
    name = this[GalaxyTable.name],
    url = this[GalaxyTable.url],
    visibility = this[GalaxyTable.visibility],
    createdAt = this[GalaxyTable.createdAt].toInstantFromUtc(),
)

internal fun UpdateBuilder<*>.writeFull(galaxy: Galaxy) {
    this[GalaxyTable.id] = galaxy.galaxyId.toUUID()
    this[GalaxyTable.hostId] = galaxy.hostId.value.toUUID()
    this[GalaxyTable.createdAt] = galaxy.createdAt.toLocalDateTimeUtc()
    writeUpdate(galaxy)
}

internal fun UpdateBuilder<*>.writeUpdate(galaxy: Galaxy) {
    this[GalaxyTable.name] = galaxy.name
    this[GalaxyTable.url] = galaxy.url
    this[GalaxyTable.visibility] = galaxy.visibility
}