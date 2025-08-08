package ponder.galaxy.server.db.services

import kabinet.utils.startOfDay
import klutch.db.DbService
import klutch.db.read
import klutch.db.readById
import klutch.utils.eq
import klutch.utils.greaterEq
import klutch.utils.toUUID
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLog
import ponder.galaxy.model.data.StarLogId
import ponder.galaxy.server.db.tables.StarLogTable
import ponder.galaxy.server.db.tables.StarTable
import ponder.galaxy.server.db.tables.toStarLog
import ponder.galaxy.server.db.tables.writeFull

class StarLogTableDao(): DbService() {

    suspend fun insert(starLog: StarLog) = dbQuery {
        StarLogTable.insertAndGetId { it.writeFull(starLog) }.value
    }

    suspend fun readById(starLogId: StarLogId) = dbQuery {
        StarLogTable.readById(starLogId.value).toStarLog()
    }

    suspend fun readAllByStarIds(starIds: List<StarId>) = dbQuery {
        StarLogTable.read { it.starId.inList(starIds.map { starId -> starId.toUUID()}) }
            .orderBy(StarLogTable.createdAt, SortOrder.ASC)
            .map { it.toStarLog() }
            .groupBy { it.starId }
    }

    suspend fun readAllByStarId(starId: StarId) = dbQuery {
        StarLogTable.read { it.starId.eq(starId)}
            .orderBy(StarLogTable.createdAt, SortOrder.ASC)
            .map { it.toStarLog() }
    }

    suspend fun readLatestByStarId(starId: StarId) = dbQuery {
        StarLogTable.read { it.starId.eq(starId) }
            .orderBy(StarLogTable.createdAt, SortOrder.DESC)
            .limit(1).firstOrNull()?.toStarLog()
    }

    suspend fun readLatestByGalaxyId(galaxyId: GalaxyId) = dbQuery {
        StarLogTable.leftJoin(StarTable).select(StarLogTable.columns)
            .where { StarTable.galaxyId.eq(galaxyId) and StarLogTable.createdAt.greaterEq(Clock.startOfDay()) }
        // not finished
    }
}