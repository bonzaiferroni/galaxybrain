@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.services

import kabinet.web.Url
import klutch.db.DbService
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.insertAndGetId
import ponder.galaxy.model.data.Host
import ponder.galaxy.model.data.HostId
import ponder.galaxy.server.db.tables.HostTable
import ponder.galaxy.server.db.tables.writeFull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class HostTableService(val dao: HostTableDao = HostTableDao()): DbService() {

    suspend fun createByUrl(url: Url): Host = dbQuery {
        val host = Host(
            hostId = HostId(Uuid.random()),
            core = url.core,
            createdAt = Clock.System.now(),
        )
        HostTable.insertAndGetId { it.writeFull(host) }
        host
    }
}
