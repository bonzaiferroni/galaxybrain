@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.services

import klutch.db.DbService
import klutch.db.readSingleOrNull
import klutch.utils.eq
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import ponder.galaxy.model.data.Galaxy
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.HostId
import ponder.galaxy.server.db.tables.GalaxyTable
import ponder.galaxy.server.db.tables.toGalaxy
import ponder.galaxy.server.db.tables.writeFull
import kotlin.uuid.ExperimentalUuidApi

class GalaxyService(
    val dao: GalaxyTableDao = GalaxyTableDao(),
): DbService() {

    suspend fun readByNameAndHostId(hostId: HostId, name: String) = dbQuery {
        GalaxyTable.readSingleOrNull { it.hostId.eq(hostId) and (it.name eq name) }?.toGalaxy()
    }

    suspend fun createByNameAndHostId(hostId: HostId, url: String, name: String): Galaxy = dbQuery {
        val galaxy = Galaxy(
            galaxyId = GalaxyId.random(),
            hostId = hostId,
            name = name,
            url = url,
            visibility = 0f,
            createdAt = Clock.System.now(),
        )
        GalaxyTable.insertAndGetId { it.writeFull(galaxy) }
        galaxy
    }
}
