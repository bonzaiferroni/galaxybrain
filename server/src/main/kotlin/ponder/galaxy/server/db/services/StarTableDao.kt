package ponder.galaxy.server.db.services

import klutch.db.DbService
import klutch.db.read
import klutch.db.readById
import klutch.db.readSingleOrNull
import klutch.db.updateSingleWhere
import klutch.utils.greaterEq
import klutch.utils.toUUID
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import ponder.galaxy.server.db.tables.StarTable
import ponder.galaxy.server.db.tables.toStar
import ponder.galaxy.server.db.tables.update
import ponder.galaxy.server.db.tables.write
import java.util.UUID

class StarTableDao: DbService() {

    suspend fun insert(vararg stars: Star) = dbQuery {
        StarTable.batchInsert(stars.toList()) { write(it) }
    }

    suspend fun upsert(vararg stars: Star) = dbQuery {
        StarTable.batchUpsert(stars.toList()) { write(it) }
    }

    suspend fun update(vararg stars: Star) = dbQuery {
        stars.forEach { star -> StarTable.update { it.update(star) } }
    }

    suspend fun delete(vararg stars: Star) = dbQuery {
        StarTable.deleteWhere { StarTable.id inList stars.map { it.starId.toUUID() } }
    }

    suspend fun updateByUrlOrInsert(url: String, provideStar: () -> Star) = dbQuery {
        val star = provideStar()
        StarTable.updateSingleWhere({ it.url.eq(url) }) {
            it.update(star)
        } ?: StarTable.insertAndGetId { it.write(star) }.value
    }

    suspend fun readVisibleStars(limit: Int, createdAfter: Instant) = dbQuery {
        StarTable.read { it.createdAt.greaterEq(createdAfter) }
            .orderBy(StarTable.visibility, SortOrder.DESC)
            .limit(limit)
            .map { it.toStar() }
    }

    suspend fun readById(starId: StarId) = dbQuery {
        StarTable.readById(starId.toUUID()).toStar()
    }

    suspend fun readByIds(starIds: List<StarId>) = dbQuery {
        StarTable.read { it.id.inList(starIds.map { starId -> starId.toUUID()}) }.map { it.toStar() }
    }
}