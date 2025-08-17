@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.services

import klutch.db.DbService
import klutch.db.batchUpdate
import klutch.db.read
import klutch.db.readByIdOrNull
import klutch.utils.toUUID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import ponder.galaxy.model.data.StarSnippet
import ponder.galaxy.model.data.StarSnippetId
import ponder.galaxy.server.db.tables.StarSnippetTable
import ponder.galaxy.server.db.tables.toStarSnippet
import ponder.galaxy.server.db.tables.writeFull
import ponder.galaxy.server.db.tables.writeUpdate
import kotlin.uuid.ExperimentalUuidApi

class StarSnippetTableDao : DbService() {

    suspend fun insert(vararg snippets: StarSnippet) = dbQuery {
        StarSnippetTable.batchInsert(snippets.toList()) { writeFull(it) }
    }

    suspend fun upsert(vararg snippets: StarSnippet) = dbQuery {
        StarSnippetTable.batchUpsert(snippets.toList()) { writeFull(it) }
    }

    suspend fun update(vararg snippets: StarSnippet) = dbQuery {
        StarSnippetTable.batchUpdate(snippets.toList(), { it.starSnippetId.value.toUUID() }) { writeUpdate(it) }
    }

    suspend fun delete(vararg snippets: StarSnippet) = dbQuery {
        StarSnippetTable.deleteWhere { StarSnippetTable.id inList snippets.map { it.starSnippetId.value.toUUID() } }
    }

    suspend fun readByIdOrNull(id: StarSnippetId) = dbQuery {
        StarSnippetTable.readByIdOrNull(id.value.toUUID())?.toStarSnippet()
    }

    suspend fun readByIds(ids: List<StarSnippetId>) = dbQuery {
        StarSnippetTable.read { it.id.inList(ids.map { it.value.toUUID() }) }.map { it.toStarSnippet() }
    }
}
