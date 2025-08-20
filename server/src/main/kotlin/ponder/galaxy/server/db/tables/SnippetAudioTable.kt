@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.tables

import klutch.utils.toStringId
import klutch.utils.toUuid
import klutch.utils.toUUID
import kotlin.uuid.ExperimentalUuidApi
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ponder.galaxy.model.data.SnippetAudio
import ponder.galaxy.model.data.SnippetId

internal object SnippetAudioTable : UUIDTable("snippet_audio") {
    val path = text("path")
}

internal fun ResultRow.toSnippetAudio() = SnippetAudio(
    snippetId = SnippetId(this[SnippetAudioTable.id].value.toStringId()),
    path = this[SnippetAudioTable.path],
)

internal fun UpdateBuilder<*>.writeFull(snippetAudio: SnippetAudio) {
    this[SnippetAudioTable.id] = snippetAudio.snippetId.value.toUUID()
    writeUpdate(snippetAudio)
}

internal fun UpdateBuilder<*>.writeUpdate(snippetAudio: SnippetAudio) {
    this[SnippetAudioTable.path] = snippetAudio.path
}
