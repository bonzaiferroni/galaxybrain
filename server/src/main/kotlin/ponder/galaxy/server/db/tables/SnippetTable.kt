@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.tables

import klutch.utils.toStringId
import klutch.utils.toUuid
import kotlin.uuid.ExperimentalUuidApi
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ponder.galaxy.model.data.Snippet
import ponder.galaxy.model.data.SnippetId

internal object SnippetTable : UUIDTable("snippet") {
    val text = text("text").uniqueIndex()
}

internal fun ResultRow.toSnippet() = Snippet(
    snippetId = SnippetId(this[SnippetTable.id].value.toStringId()),
    text = this[SnippetTable.text],
)

internal fun UpdateBuilder<*>.writeFull(snippet: Snippet) {
    this[SnippetTable.id] = snippet.snippetId.value.toUUID()
    writeUpdate(snippet)
}

internal fun UpdateBuilder<*>.writeUpdate(snippet: Snippet) {
    this[SnippetTable.text] = snippet.text
}
