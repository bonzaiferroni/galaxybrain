@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.services

import klutch.db.DbService
import klutch.db.batchUpdate
import klutch.db.read
import klutch.utils.eq
import klutch.utils.toUUID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
import ponder.galaxy.model.data.Signal
import ponder.galaxy.model.data.SignalId
import ponder.galaxy.model.data.SignalScanId
import ponder.galaxy.model.data.StarSnippetId
import ponder.galaxy.server.db.tables.SignalTable
import ponder.galaxy.server.db.tables.toSignal
import ponder.galaxy.server.db.tables.writeFull
import ponder.galaxy.server.db.tables.writeUpdate
import kotlin.uuid.ExperimentalUuidApi

class SignalTableDao : DbService() {

    suspend fun insert(signal: Signal) = dbQuery {
        SignalTable.insert { it.writeFull(signal) }
    }

    suspend fun insert(signals: List<Signal>) = dbQuery {
        SignalTable.batchInsert(signals) { writeFull(it) }
    }

    suspend fun upsert(signal: Signal) = dbQuery {
        SignalTable.upsert { it.writeFull(signal) }
    }

    suspend fun upsert(signals: List<Signal>) = dbQuery {
        SignalTable.batchUpsert(signals) { writeFull(it) }
    }

    suspend fun update(signal: Signal) = dbQuery {
        SignalTable.update { it.writeUpdate(signal) }
    }

    suspend fun update(signals: List<Signal>) = dbQuery {
        SignalTable.batchUpdate(signals, { it.signalId.value.toUUID() }) { writeUpdate(it) }
    }

    suspend fun delete(vararg signals: Signal) = dbQuery {
        SignalTable.deleteWhere { SignalTable.id inList signals.map { it.signalId.value.toUUID() } }
    }

    suspend fun readBySignalScanId(signalScanId: SignalScanId) = dbQuery {
        SignalTable.read { it.signalScanId.eq(signalScanId) }.map { it.toSignal() }
    }

    suspend fun readByStarSnippetId(starSnippetId: StarSnippetId) = dbQuery {
        SignalTable.read { it.starSnippetId.eq(starSnippetId) }.map { it.toSignal() }
    }
}
