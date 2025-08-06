package ponder.galaxy.server.db.services

import klutch.db.DbService
import klutch.db.read
import klutch.db.readById
import klutch.utils.eq
import klutch.utils.toUUID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insertAndGetId
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLog
import ponder.galaxy.model.data.StarLogId
import ponder.galaxy.server.db.tables.StarLogTable
import ponder.galaxy.server.db.tables.toStarLog
import ponder.galaxy.server.db.tables.writeFull

class StarLogTableDao(): DbService() {

    suspend fun insert(starLog: StarLog) = dbQuery {
        StarLogTable.insertAndGetId { it.writeFull(starLog) }.value
    }

    suspend fun readById(starLogId: StarLogId) = dbQuery {
        StarLogTable.readById(starLogId.value).toStarLog()
    }

    suspend fun readLogsByStarIds(starIds: List<StarId>) = dbQuery {
        StarLogTable.read { it.starId.inList(starIds.map { starId -> starId.toUUID()}) }
            .orderBy(StarLogTable.createdAt, SortOrder.ASC)
            .map { it.toStarLog() }
            .groupBy { it.starId }
    }

    suspend fun readLatestByStarId(starId: StarId) = dbQuery {
        StarLogTable.read { it.starId.eq(starId) }
            .orderBy(StarLogTable.createdAt, SortOrder.DESC)
            .limit(1).firstOrNull()?.toStarLog()
    }
}