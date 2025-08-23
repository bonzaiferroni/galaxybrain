@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package ponder.galaxy.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ponder.galaxy.model.data.HostId
import ponder.galaxy.model.data.LinkVisit
import ponder.galaxy.model.data.LinkVisitId
import ponder.galaxy.model.data.LinkVisitOutcome

internal object LinkVisitTable : LongIdTable("link_visit") {
    val hostId = reference("host_id", HostTable, onDelete = ReferenceOption.CASCADE)
    val url = text("url")
    val outcome = enumerationByName("outcome", 32, LinkVisitOutcome::class)
    val createdAt = datetime("created_at")
}

internal fun ResultRow.toLinkVisit() = LinkVisit(
    linkVisitId = LinkVisitId(this[LinkVisitTable.id].value),
    hostId = HostId(this[LinkVisitTable.hostId].value.toStringId()),
    url = this[LinkVisitTable.url],
    outcome = this[LinkVisitTable.outcome],
    createdAt = this[LinkVisitTable.createdAt].toInstantFromUtc(),
)

internal fun UpdateBuilder<*>.writeFull(visit: LinkVisit) {
    this[LinkVisitTable.hostId] = visit.hostId.value.toUUID()
    this[LinkVisitTable.url] = visit.url
    this[LinkVisitTable.outcome] = visit.outcome
    this[LinkVisitTable.createdAt] = visit.createdAt.toLocalDateTimeUtc()
}
