@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.services

import klutch.db.DbService
import klutch.db.readSingleOrNull
import klutch.utils.eq
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import kabinet.utils.generateUuidString
import ponder.galaxy.model.data.Galaxy
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.HostId
import ponder.galaxy.server.db.tables.GalaxyTable
import ponder.galaxy.server.db.tables.toGalaxy
import ponder.galaxy.server.db.tables.writeFull
import kotlin.uuid.ExperimentalUuidApi

class GalaxyService(val dao: GalaxyTableDao = GalaxyTableDao()): DbService() {

    suspend fun readUnchartedByHostId(hostId: HostId) = dbQuery {
        GalaxyTable.readSingleOrNull { it.hostId.eq(hostId) and (it.name eq "Uncharted") }?.toGalaxy()
    }

    suspend fun createUnchartedByHostId(hostId: HostId, url: String): Galaxy = dbQuery {
        val galaxy = Galaxy(
            galaxyId = GalaxyId(generateUuidString()),
            hostId = hostId,
            name = "Uncharted",
            url = url,
            visibility = 0f,
            createdAt = Clock.System.now(),
        )
        GalaxyTable.insertAndGetId { it.writeFull(galaxy) }
        galaxy
    }
}
