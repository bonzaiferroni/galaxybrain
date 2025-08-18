package ponder.galaxy.model.data

import kabinet.db.TableId
import kabinet.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Host(
    val hostId: HostId,
    val core: String,
    val createdAt: Instant,
)

@JvmInline
@Serializable
value class HostId(override val value: String) : TableId<String> {
    companion object {
        fun random() = HostId(randomUuidString())
    }
}

