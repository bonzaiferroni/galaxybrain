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
import ponder.galaxy.model.data.SignalScan
import ponder.galaxy.model.data.SignalScanId
import ponder.galaxy.model.data.UniverseId
import ponder.galaxy.server.db.tables.SignalScanTable
import ponder.galaxy.server.db.tables.toSignalScan
import ponder.galaxy.server.db.tables.writeFull
import ponder.galaxy.server.db.tables.writeUpdate
import kotlin.uuid.ExperimentalUuidApi

class SignalScanTableDao : DbService() {

    suspend fun insert(scan: SignalScan) = dbQuery {
        SignalScanTable.insert { it.writeFull(scan) }
    }

    suspend fun insert(scans: List<SignalScan>) = dbQuery {
        SignalScanTable.batchInsert(scans) { writeFull(it) }
    }

    suspend fun upsert(scan: SignalScan) = dbQuery {
        SignalScanTable.upsert { it.writeFull(scan) }
    }

    suspend fun upsert(scans: List<SignalScan>) = dbQuery {
        SignalScanTable.batchUpsert(scans) { writeFull(it) }
    }

    suspend fun update(scan: SignalScan) = dbQuery {
        SignalScanTable.update { it.writeUpdate(scan) }
    }

    suspend fun update(scans: List<SignalScan>) = dbQuery {
        SignalScanTable.batchUpdate(scans, { it.signalScanId.value.toUUID() }) { writeUpdate(it) }
    }

    suspend fun delete(vararg scans: SignalScan) = dbQuery {
        SignalScanTable.deleteWhere { SignalScanTable.id inList scans.map { it.signalScanId.value.toUUID() } }
    }

    suspend fun readByUniverseId(universeId: UniverseId) = dbQuery {
        SignalScanTable.read { it.universeId.eq(universeId) }.map { it.toSignalScan() }
    }

    suspend fun readById(signalScanId: SignalScanId) = dbQuery {
        SignalScanTable.read { it.id.eq(signalScanId) }.map { it.toSignalScan() }.firstOrNull()
    }
}
