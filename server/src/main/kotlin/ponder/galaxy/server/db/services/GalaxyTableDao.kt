package ponder.galaxy.server.db.services

import klutch.db.DbService
import klutch.db.readById
import klutch.db.readSingleOrNull
import klutch.db.updateSingleWhere
import klutch.utils.eq
import klutch.utils.toUUID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import ponder.galaxy.model.data.Galaxy
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.server.db.tables.GalaxyTable
import ponder.galaxy.server.db.tables.toGalaxy
import ponder.galaxy.server.db.tables.writeFull
import ponder.galaxy.server.db.tables.writeUpdate

class GalaxyTableDao : DbService() {

    suspend fun insert(vararg galaxys: Galaxy) = dbQuery {
        GalaxyTable.batchInsert(galaxys.toList()) { writeFull(it) }
    }

    suspend fun upsert(vararg galaxys: Galaxy) = dbQuery {
        GalaxyTable.batchUpsert(galaxys.toList()) { writeFull(it) }
    }

    suspend fun update(vararg galaxys: Galaxy) = dbQuery {
        galaxys.forEach { galaxy ->
            GalaxyTable.updateSingleWhere({ it.id.eq(galaxy.galaxyId)}) { it.writeUpdate(galaxy) }
        }
    }

    suspend fun delete(vararg galaxys: Galaxy) = dbQuery {
        GalaxyTable.deleteWhere { GalaxyTable.id inList galaxys.map { it.galaxyId.toUUID() } }
    }

    suspend fun readByNameOrInsert(name: String, provideGalaxy: () -> Galaxy) = dbQuery {
        val resultRow = GalaxyTable.readSingleOrNull { it.name eq name }
            ?: GalaxyTable.insertAndGetId { it.writeFull(provideGalaxy()) }
                .let { GalaxyTable.readById(it.value) }
        resultRow.toGalaxy()
    }

    suspend fun readByName(name: String) = dbQuery {
        GalaxyTable.readSingleOrNull { it.name eq name }?.toGalaxy()
    }

    suspend fun readAll() = dbQuery {
        GalaxyTable.selectAll().map { it.toGalaxy() }
    }

    suspend fun readById(galaxyId: GalaxyId) = dbQuery {
        GalaxyTable.readById(galaxyId.toUUID()).toGalaxy()
    }
}