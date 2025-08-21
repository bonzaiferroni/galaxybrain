@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.services

import klutch.db.DbService
import klutch.db.batchUpdate
import klutch.db.read
import klutch.db.readByIdOrNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import klutch.utils.toUUID
import ponder.galaxy.model.data.SnippetEmbedding
import ponder.galaxy.model.data.SnippetId
import ponder.galaxy.server.db.tables.SnippetEmbeddingTable
import ponder.galaxy.server.db.tables.toSnippetEmbedding
import ponder.galaxy.server.db.tables.writeFull
import ponder.galaxy.server.db.tables.writeUpdate
import kotlin.uuid.ExperimentalUuidApi

class SnippetEmbeddingTableDao : DbService() {

    suspend fun insert(vararg embeddings: SnippetEmbedding) = dbQuery {
        SnippetEmbeddingTable.batchInsert(embeddings.toList()) { writeFull(it) }
    }

    suspend fun upsert(vararg embeddings: SnippetEmbedding) = dbQuery {
        SnippetEmbeddingTable.batchUpsert(embeddings.toList()) { writeFull(it) }
    }

    suspend fun update(vararg embeddings: SnippetEmbedding) = dbQuery {
        SnippetEmbeddingTable.batchUpdate(embeddings.toList(), { it.snippetId.value.toUUID() }) { writeUpdate(it) }
    }

    suspend fun delete(vararg embeddings: SnippetEmbedding) = dbQuery {
        SnippetEmbeddingTable.deleteWhere { SnippetEmbeddingTable.id inList embeddings.map { it.snippetId.value.toUUID() } }
    }

    suspend fun readByIdOrNull(snippetId: SnippetId) = dbQuery {
        SnippetEmbeddingTable.readByIdOrNull(snippetId.value.toUUID())?.toSnippetEmbedding()
    }

    suspend fun readByIds(ids: List<SnippetId>) = dbQuery {
        SnippetEmbeddingTable.read { it.id.inList(ids.map { id -> id.value.toUUID() }) }.map { it.toSnippetEmbedding() }
    }
}
