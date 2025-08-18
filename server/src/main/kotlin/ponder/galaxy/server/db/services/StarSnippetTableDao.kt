@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.services

import klutch.db.DbService
import klutch.db.batchUpdate
import klutch.db.read
import klutch.db.readByIdOrNull
import klutch.utils.eq
import klutch.utils.toStringId
import klutch.utils.toUUID
import klutch.utils.toUuid
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import ponder.galaxy.model.data.CommentId
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarSnippet
import ponder.galaxy.model.data.StarSnippetId
import ponder.galaxy.server.db.tables.SnippetTable
import ponder.galaxy.server.db.tables.StarSnippetTable
import ponder.galaxy.server.db.tables.toSnippet
import ponder.galaxy.server.db.tables.toStarSnippet
import ponder.galaxy.server.db.tables.writeFull
import ponder.galaxy.server.db.tables.writeUpdate
import kotlin.uuid.ExperimentalUuidApi

class StarSnippetTableDao : DbService() {

    suspend fun insert(snippet: StarSnippet) = dbQuery {
        StarSnippetTable.insert { it.writeFull(snippet) }
    }

    suspend fun insert(snippets: List<StarSnippet>) = dbQuery {
        StarSnippetTable.batchInsert(snippets) { writeFull(it) }
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
        StarSnippetTable.read { table -> table.id.inList(ids.map { it.value.toUUID() }) }.map { it.toStarSnippet() }
    }

    suspend fun readByCommentId(commentId: CommentId) = dbQuery {
        StarSnippetTable.join(SnippetTable, JoinType.INNER, StarSnippetTable.snippetId, SnippetTable.id)
            .select(SnippetTable.columns)
            .where { StarSnippetTable.commentId.eq(commentId) }
            .orderBy(StarSnippetTable.order, SortOrder.ASC_NULLS_LAST)
            .map { it.toSnippet() }
    }

    suspend fun readCommentSnippetsByStarId(starId: StarId) = dbQuery {
        StarSnippetTable.join(SnippetTable, JoinType.INNER, StarSnippetTable.snippetId, SnippetTable.id)
            .select(SnippetTable.columns + StarSnippetTable.commentId)
            .where { StarSnippetTable.commentId.isNotNull() and StarSnippetTable.starId.eq(starId) }
            .orderBy(StarSnippetTable.order, SortOrder.ASC_NULLS_LAST)
            .map { CommentId(it[StarSnippetTable.commentId]!!.value.toStringId()) to it.toSnippet() }
            .groupBy({ it.first }, { it.second })
    }

    suspend fun readByCommentIds(commentIds: List<CommentId>) = dbQuery {
        StarSnippetTable.join(SnippetTable, JoinType.INNER, StarSnippetTable.snippetId, SnippetTable.id)
            .select(SnippetTable.columns + StarSnippetTable.commentId)
            .where { StarSnippetTable.commentId.inList(commentIds.map { it.toUUID() }) }
            .orderBy(StarSnippetTable.order, SortOrder.ASC_NULLS_LAST)
            .map { CommentId(it[StarSnippetTable.commentId]!!.value.toStringId()) to it.toSnippet() }
            .groupBy({ it.first }, { it.second })
    }
}
