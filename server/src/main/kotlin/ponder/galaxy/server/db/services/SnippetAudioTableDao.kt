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
import ponder.galaxy.model.data.SnippetAudio
import ponder.galaxy.model.data.SnippetId
import ponder.galaxy.server.db.tables.SnippetAudioTable
import ponder.galaxy.server.db.tables.toSnippetAudio
import ponder.galaxy.server.db.tables.writeFull
import ponder.galaxy.server.db.tables.writeUpdate
import kotlin.uuid.ExperimentalUuidApi

class SnippetAudioTableDao : DbService() {

    suspend fun insert(vararg items: SnippetAudio) = dbQuery {
        SnippetAudioTable.batchInsert(items.toList()) { writeFull(it) }
    }

    suspend fun upsert(vararg items: SnippetAudio) = dbQuery {
        SnippetAudioTable.batchUpsert(items.toList()) { writeFull(it) }
    }

    suspend fun update(vararg items: SnippetAudio) = dbQuery {
        SnippetAudioTable.batchUpdate(items.toList(), { it.snippetId.value.toUUID() }) { writeUpdate(it) }
    }

    suspend fun delete(vararg items: SnippetAudio) = dbQuery {
        SnippetAudioTable.deleteWhere { SnippetAudioTable.id inList items.map { it.snippetId.value.toUUID() } }
    }

    suspend fun readByIdOrNull(id: SnippetId) = dbQuery {
        SnippetAudioTable.readByIdOrNull(id.value.toUUID())?.toSnippetAudio()
    }

    suspend fun readByIds(ids: List<SnippetId>) = dbQuery {
        SnippetAudioTable.read { it.id.inList(ids.map { id -> id.value.toUUID() }) }.map { it.toSnippetAudio() }
    }
}
