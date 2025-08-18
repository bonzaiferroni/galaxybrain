@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.services

import kabinet.web.Url
import klutch.db.DbService
import klutch.db.batchUpdate
import klutch.db.eq
import klutch.db.read
import klutch.db.readByIdOrNull
import klutch.db.readSingleOrNull
import klutch.utils.eq
import klutch.utils.toUUID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLink
import ponder.galaxy.model.data.StarLinkId
import ponder.galaxy.server.db.tables.StarLinkTable
import ponder.galaxy.server.db.tables.StarTable
import ponder.galaxy.server.db.tables.toStar
import ponder.galaxy.server.db.tables.toStarLink
import ponder.galaxy.server.db.tables.writeFull
import ponder.galaxy.server.db.tables.writeUpdate
import kotlin.uuid.ExperimentalUuidApi

class StarLinkTableDao : DbService() {

    suspend fun insert(starLink: StarLink) = dbQuery {
        StarLinkTable.insert { it.writeFull(starLink) }
    }

    suspend fun insert(starLinks: List<StarLink>) = dbQuery {
        StarLinkTable.batchInsert(starLinks.toList()) { writeFull(it) }
    }

    suspend fun upsert(vararg starLinks: StarLink) = dbQuery {
        StarLinkTable.batchUpsert(starLinks.toList()) { writeFull(it) }
    }

    suspend fun update(vararg starLinks: StarLink) = dbQuery {
        StarLinkTable.batchUpdate(starLinks.toList(), { it.starLinkId.toUUID() }) { writeUpdate(it) }
    }

    suspend fun delete(vararg starLinks: StarLink) = dbQuery {
        StarLinkTable.deleteWhere { StarLinkTable.id inList starLinks.map { it.starLinkId.toUUID() } }
    }

    suspend fun readByIdOrNull(starLinkId: StarLinkId) = dbQuery {
        StarLinkTable.readByIdOrNull(starLinkId.toUUID())?.toStarLink()
    }

    suspend fun readByIds(starLinkIds: List<StarLinkId>) = dbQuery {
        StarLinkTable.read { it.id.inList(starLinkIds.map { id -> id.toUUID() }) }.map { it.toStarLink() }
    }

    suspend fun readByUrl(url: Url) = dbQuery {
        StarLinkTable.readSingleOrNull { it.url.eq(url) }?.toStarLink()
    }

    suspend fun readOutgoingByStarId(starId: StarId) = dbQuery {
        StarLinkTable.read { it.fromStarId.eq(starId) }.map { it.toStarLink() }
    }
}
