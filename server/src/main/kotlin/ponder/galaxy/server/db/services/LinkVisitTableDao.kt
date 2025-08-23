package ponder.galaxy.server.db.services

import klutch.db.DbService
import klutch.db.batchUpdate
import klutch.db.read
import klutch.db.readByIdOrNull
import klutch.db.readSingleOrNull
import klutch.utils.eq
import klutch.utils.greaterEq
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import ponder.galaxy.model.data.HostId
import ponder.galaxy.model.data.LinkVisit
import ponder.galaxy.model.data.LinkVisitId
import ponder.galaxy.server.db.tables.LinkVisitTable
import ponder.galaxy.server.db.tables.toLinkVisit
import ponder.galaxy.server.db.tables.writeFull
import kotlin.time.Duration.Companion.days

class LinkVisitTableDao: DbService() {

    suspend fun insert(vararg visits: LinkVisit) = dbQuery {
        LinkVisitTable.batchInsert(visits.toList()) { writeFull(it) }
    }

    suspend fun update(vararg visits: LinkVisit) = dbQuery {
        LinkVisitTable.batchUpdate(visits.toList(), { it.linkVisitId.value }) { writeFull(it) }
    }

    suspend fun delete(vararg visits: LinkVisit) = dbQuery {
        LinkVisitTable.deleteWhere { LinkVisitTable.id inList visits.map { it.linkVisitId.value } }
    }

    suspend fun readByIdOrNull(linkVisitId: LinkVisitId) = dbQuery {
        LinkVisitTable.readByIdOrNull(linkVisitId.value)?.toLinkVisit()
    }

    suspend fun readByIds(linkVisitIds: List<LinkVisitId>) = dbQuery {
        LinkVisitTable.read { it.id.inList(linkVisitIds.map { id -> id.value }) }.map { it.toLinkVisit() }
    }

    suspend fun readByUrl(url: String) = dbQuery {
        LinkVisitTable.readSingleOrNull { it.url.eq(url) }?.toLinkVisit()
    }
}
