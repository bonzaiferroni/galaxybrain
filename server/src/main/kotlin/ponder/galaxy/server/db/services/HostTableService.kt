package ponder.galaxy.server.db.services

import kabinet.web.Url
import klutch.db.DbService
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.insertAndGetId
import ponder.galaxy.model.data.Host
import ponder.galaxy.model.data.HostId
import ponder.galaxy.server.db.tables.HostTable
import ponder.galaxy.server.db.tables.writeFull

class HostTableService(val dao: HostTableDao = HostTableDao()): DbService() {

    suspend fun createByUrl(url: Url): Host = dbQuery {
        val host = Host(
            hostId = HostId.random(),
            core = url.core,
            createdAt = Clock.System.now(),
        )
        HostTable.insertAndGetId { it.writeFull(host) }
        host
    }
}
