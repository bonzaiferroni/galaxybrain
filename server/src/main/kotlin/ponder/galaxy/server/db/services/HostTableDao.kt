@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package ponder.galaxy.server.db.services

import kabinet.web.Url
import klutch.db.DbService
import klutch.db.batchUpdate
import klutch.db.read
import klutch.db.readByIdOrNull
import klutch.db.readSingleOrNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import ponder.galaxy.model.data.Host
import ponder.galaxy.model.data.HostId
import ponder.galaxy.server.db.tables.HostTable
import ponder.galaxy.server.db.tables.toHost
import ponder.galaxy.server.db.tables.writeFull
import ponder.galaxy.server.db.tables.writeUpdate
import klutch.utils.toUUID

class HostTableDao : DbService() {

    suspend fun insert(vararg hosts: Host) = dbQuery {
        HostTable.batchInsert(hosts.toList()) { writeFull(it) }
    }

    suspend fun upsert(vararg hosts: Host) = dbQuery {
        HostTable.batchUpsert(hosts.toList()) { writeFull(it) }
    }

    suspend fun update(vararg hosts: Host) = dbQuery {
        HostTable.batchUpdate(hosts.toList(), { it.hostId.value.toUUID() }) { writeUpdate(it) }
    }

    suspend fun delete(vararg hosts: Host) = dbQuery {
        HostTable.deleteWhere { HostTable.id inList hosts.map { it.hostId.value.toUUID() } }
    }

    suspend fun readByIdOrNull(hostId: HostId) = dbQuery {
        HostTable.readByIdOrNull(hostId.value.toUUID())?.toHost()
    }

    suspend fun readByIds(hostIds: List<HostId>) = dbQuery {
        HostTable.read { it.id.inList(hostIds.map { id -> id.value.toUUID() }) }.map { it.toHost() }
    }

    suspend fun readByUrl(url: Url) = dbQuery {
        HostTable.readSingleOrNull { it.core.eq(url.core) }?.toHost()
    }

    suspend fun readByCore(core: String) = dbQuery {
        HostTable.readSingleOrNull { it.core.eq(core) }?.toHost()
    }
}
