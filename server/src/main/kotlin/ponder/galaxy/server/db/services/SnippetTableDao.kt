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
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SortOrder
import ponder.galaxy.model.data.Snippet
import ponder.galaxy.model.data.SnippetId
import ponder.galaxy.model.data.StarId
import ponder.galaxy.server.db.tables.CommentTable
import ponder.galaxy.server.db.tables.SnippetTable
import ponder.galaxy.server.db.tables.StarSnippetTable
import ponder.galaxy.server.db.tables.toSnippet
import ponder.galaxy.server.db.tables.writeFull
import ponder.galaxy.server.db.tables.writeUpdate
import kotlin.uuid.ExperimentalUuidApi

class SnippetTableDao : DbService() {

    suspend fun insert(vararg contents: Snippet) = dbQuery {
        SnippetTable.batchInsert(contents.toList()) { writeFull(it) }
    }

    suspend fun upsert(vararg contents: Snippet) = dbQuery {
        SnippetTable.batchUpsert(contents.toList()) { writeFull(it) }
    }

    suspend fun update(vararg contents: Snippet) = dbQuery {
        SnippetTable.batchUpdate(contents.toList(), { it.snippetId.value.toUUID() }) { writeUpdate(it) }
    }

    suspend fun delete(vararg contents: Snippet) = dbQuery {
        SnippetTable.deleteWhere { SnippetTable.id inList contents.map { it.snippetId.value.toUUID() } }
    }

    suspend fun readByIdOrNull(id: SnippetId) = dbQuery {
        SnippetTable.readByIdOrNull(id.value.toUUID())?.toSnippet()
    }

    suspend fun readByIds(ids: List<SnippetId>) = dbQuery {
        SnippetTable.read { it.id.inList(ids.map { id -> id.value.toUUID() }) }.map { it.toSnippet() }
    }

    suspend fun readTextByStarId(starId: StarId): List<String> = dbQuery {
        StarSnippetTable.innerJoin(SnippetTable)
            .select(SnippetTable.text)
            .where { StarSnippetTable.starId.eq(starId) }
            .orderBy(StarSnippetTable.index)
            .map { it[SnippetTable.text] }
    }
}
