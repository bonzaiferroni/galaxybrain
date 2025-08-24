@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.services

import klutch.db.DbService
import klutch.db.batchUpdate
import klutch.db.read
import klutch.db.readByIdOrNull
import klutch.utils.eq
import klutch.utils.toUUID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import ponder.galaxy.model.data.QuestionId
import ponder.galaxy.model.data.Universe
import ponder.galaxy.model.data.UniverseId
import ponder.galaxy.server.db.tables.UniverseTable
import ponder.galaxy.server.db.tables.toUniverse
import ponder.galaxy.server.db.tables.writeFull
import ponder.galaxy.server.db.tables.writeUpdate
import kotlin.uuid.ExperimentalUuidApi

class UniverseTableDao : DbService() {

    suspend fun insert(vararg universes: Universe) = dbQuery {
        UniverseTable.batchInsert(universes.toList()) { writeFull(it) }
    }

    suspend fun upsert(vararg universes: Universe) = dbQuery {
        UniverseTable.batchUpsert(universes.toList()) { writeFull(it) }
    }

    suspend fun update(vararg universes: Universe) = dbQuery {
        UniverseTable.batchUpdate(universes.toList(), { it.universeId.value.toUUID() }) { writeUpdate(it) }
    }

    suspend fun delete(vararg universes: Universe) = dbQuery {
        UniverseTable.deleteWhere { UniverseTable.id inList universes.map { it.universeId.value.toUUID() } }
    }

    suspend fun readByIdOrNull(universeId: UniverseId) = dbQuery {
        UniverseTable.readByIdOrNull(universeId.value.toUUID())?.toUniverse()
    }

    suspend fun readByIds(universeIds: List<UniverseId>) = dbQuery {
        UniverseTable.read { it.id.inList(universeIds.map { id -> id.value.toUUID() }) }.map { it.toUniverse() }
    }

    suspend fun readByQuestion(questionId: QuestionId) = dbQuery {
        UniverseTable.read { it.questionId.eq(questionId.value.toUUID()) }.map { it.toUniverse() }
    }

    suspend fun countByQuestionId(questionId: QuestionId) = dbQuery {
        UniverseTable.read { it.questionId.eq(questionId) }.count()
    }
}
