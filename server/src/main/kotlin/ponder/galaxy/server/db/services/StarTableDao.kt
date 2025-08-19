@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.services

import kabinet.web.Url
import klutch.db.DbService
import klutch.db.batchUpdate
import klutch.db.read
import klutch.db.readByIdOrNull
import klutch.db.readSingleOrNull
import klutch.db.updateSingleWhere
import klutch.utils.eq
import klutch.utils.greaterEq
import klutch.utils.toUUID
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import ponder.galaxy.server.db.tables.StarTable
import ponder.galaxy.server.db.tables.toStar
import ponder.galaxy.server.db.tables.writeFull
import ponder.galaxy.server.db.tables.writeUpdate
import kotlin.uuid.ExperimentalUuidApi

class StarTableDao: DbService() {

    suspend fun insert(vararg stars: Star) = dbQuery {
        StarTable.batchInsert(stars.toList()) { writeFull(it) }
    }

    suspend fun upsert(vararg stars: Star) = dbQuery {
        StarTable.batchUpsert(stars.toList()) { writeFull(it) }
    }

    suspend fun update(vararg stars: Star) = dbQuery {
        StarTable.batchUpdate(stars.toList(), { it.starId.toUUID()}) { writeUpdate(it) }
    }

    suspend fun delete(vararg stars: Star) = dbQuery {
        StarTable.deleteWhere { StarTable.id inList stars.map { it.starId.toUUID() } }
    }

    suspend fun updateByUrlOrInsert(url: String, galaxyId: GalaxyId, provideStar: () -> Star) = dbQuery {
        val star = provideStar()
        StarTable.updateSingleWhere({ it.link.eq(url) and it.galaxyId.eq(galaxyId) }) {
            it.writeUpdate(star)
        } ?: StarTable.insertAndGetId { it.writeFull(star) }.value
    }

    suspend fun readVisibleStars(limit: Int, createdAfter: Instant) = dbQuery {
        StarTable.read { it.createdAt.greaterEq(createdAfter) }
            .orderBy(StarTable.visibility, SortOrder.DESC)
            .limit(limit)
            .map { it.toStar() }
    }

    suspend fun readByIdOrNull(starId: StarId) = dbQuery {
        StarTable.readByIdOrNull(starId.toUUID())?.toStar()
    }

    suspend fun readByIds(starIds: List<StarId>) = dbQuery {
        StarTable.read { it.id.inList(starIds.map { starId -> starId.toUUID()}) }.map { it.toStar() }
    }

    suspend fun readByUrl(url: Url) = dbQuery {
        StarTable.readSingleOrNull { it.url.eq(url.href) }?.toStar()
    }

    suspend fun readByUrl(url: String) = dbQuery {
        StarTable.readSingleOrNull { it.url.eq(url) }?.toStar()
    }

    suspend fun readLatestByGalaxyId(galaxyId: GalaxyId, limit: Int = 100) = dbQuery {
        StarTable.read { it.galaxyId.eq(galaxyId) }
            .orderBy(StarTable.createdAt, SortOrder.ASC)
            .limit(limit)
            .map { it.toStar() }
    }
}