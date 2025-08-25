@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ponder.galaxy.model.data.SignalScan
import ponder.galaxy.model.data.SignalScanId
import ponder.galaxy.model.data.UniverseId
import kotlin.uuid.ExperimentalUuidApi

internal object SignalScanTable : UUIDTable("signal_scan") {
    val universeId = reference("universe_id", UniverseTable, onDelete = ReferenceOption.CASCADE).index()
    val sum = float("sum")
    val count = integer("count")
    val createdAt = datetime("created_at")
}

internal fun ResultRow.toSignalScan() = SignalScan(
    signalScanId = SignalScanId(this[SignalScanTable.id].value.toStringId()),
    universeId = UniverseId(this[SignalScanTable.universeId].value.toStringId()),
    sum = this[SignalScanTable.sum],
    count = this[SignalScanTable.count],
    createdAt = this[SignalScanTable.createdAt].toInstantFromUtc(),
)

internal fun UpdateBuilder<*>.writeFull(signalScan: SignalScan) {
    this[SignalScanTable.id] = signalScan.signalScanId.value.toUUID()
    this[SignalScanTable.universeId] = signalScan.universeId.value.toUUID()
    this[SignalScanTable.createdAt] = signalScan.createdAt.toLocalDateTimeUtc()
    writeUpdate(signalScan)
}

internal fun UpdateBuilder<*>.writeUpdate(signalScan: SignalScan) {
    this[SignalScanTable.sum] = signalScan.sum
    this[SignalScanTable.count] = signalScan.count
}
