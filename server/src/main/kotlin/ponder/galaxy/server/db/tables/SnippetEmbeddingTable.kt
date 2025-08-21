package ponder.galaxy.server.db.tables

import klutch.db.vector
import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ponder.galaxy.model.data.SnippetEmbedding
import ponder.galaxy.model.data.SnippetId

internal object SnippetEmbeddingTable : UUIDTable("snippet_embedding") {
    val vector = vector("vector", size = 768)
}

internal fun ResultRow.toSnippetEmbedding() = SnippetEmbedding(
    snippetId = SnippetId(this[SnippetEmbeddingTable.id].value.toStringId()),
    vector = this[SnippetEmbeddingTable.vector],
)

internal fun UpdateBuilder<*>.writeFull(embedding: SnippetEmbedding) {
    this[SnippetEmbeddingTable.id] = embedding.snippetId.value.toUUID()
    writeUpdate(embedding)
}

internal fun UpdateBuilder<*>.writeUpdate(embedding: SnippetEmbedding) {
    this[SnippetEmbeddingTable.vector] = embedding.vector
}
