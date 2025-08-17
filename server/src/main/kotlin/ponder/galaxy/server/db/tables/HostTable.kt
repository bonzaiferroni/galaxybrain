@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package ponder.galaxy.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.utils.toUUID
import klutch.utils.toUuid
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ponder.galaxy.model.data.Host
import ponder.galaxy.model.data.HostId

internal object HostTable : UUIDTable("host") {
    val core = text("core")
    val createdAt = datetime("created_at")
}

internal fun ResultRow.toHost() = Host(
    hostId = HostId(this[HostTable.id].value.toUuid()),
    core = this[HostTable.core],
    createdAt = this[HostTable.createdAt].toInstantFromUtc(),
)

internal fun UpdateBuilder<*>.writeFull(host: Host) {
    this[HostTable.id] = host.hostId.value.toUUID()
    this[HostTable.createdAt] = host.createdAt.toLocalDateTimeUtc()
    writeUpdate(host)
}

internal fun UpdateBuilder<*>.writeUpdate(host: Host) {
    this[HostTable.core] = host.core
}
