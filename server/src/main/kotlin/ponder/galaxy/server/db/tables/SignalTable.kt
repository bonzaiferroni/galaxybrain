@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.utils.toStringId
import klutch.utils.toUUID
import klutch.utils.toUuid
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ponder.galaxy.model.data.Signal
import ponder.galaxy.model.data.SignalId
import ponder.galaxy.model.data.SignalScanId
import ponder.galaxy.model.data.StarSnippetId
import kotlin.uuid.ExperimentalUuidApi

internal object SignalTable : UUIDTable("signal") {
    val starSnippetId = reference("star_snippet_id", StarSnippetTable, onDelete = ReferenceOption.CASCADE).index()
    val signalScanId = reference("signal_scan_id", SignalScanTable, onDelete = ReferenceOption.CASCADE).index()
    val distance = float("distance")
    val visibility = float("visibility")
    val createdAt = datetime("created_at")
}

internal fun ResultRow.toSignal() = Signal(
    signalId = SignalId(this[SignalTable.id].value.toStringId()),
    starSnippetId = StarSnippetId(this[SignalTable.starSnippetId].value.toStringId()),
    signalScanId = SignalScanId(this[SignalTable.signalScanId].value.toStringId()),
    distance = this[SignalTable.distance],
    visibility = this[SignalTable.visibility],
    createdAt = this[SignalTable.createdAt].toInstantFromUtc(),
)

internal fun UpdateBuilder<*>.writeFull(signal: Signal) {
    this[SignalTable.id] = signal.signalId.value.toUUID()
    this[SignalTable.starSnippetId] = signal.starSnippetId.value.toUUID()
    this[SignalTable.signalScanId] = signal.signalScanId.value.toUUID()
    this[SignalTable.createdAt] = signal.createdAt.toLocalDateTimeUtc()
    writeUpdate(signal)
}

internal fun UpdateBuilder<*>.writeUpdate(signal: Signal) {
    this[SignalTable.distance] = signal.distance
    this[SignalTable.visibility] = signal.visibility
}
