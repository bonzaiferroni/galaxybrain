package ponder.galaxy.model.data

import kabinet.db.TableId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class LinkVisit(
    val linkVisitId: LinkVisitId,
    val hostId: HostId,
    val url: String,
    val outcome: LinkVisitOutcome,
    val createdAt: Instant,
)

@JvmInline @Serializable
value class LinkVisitId(override val value: Long): TableId<Long>

enum class LinkVisitOutcome {
    Skipped,
    NotFound,
    NotAllowed,
    OkNoContent,
    OkContent,
}