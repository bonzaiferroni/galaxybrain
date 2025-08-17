@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package ponder.galaxy.model.data

import kabinet.db.TableId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
data class Host(
    val hostId: HostId,
    val core: String,
    val createdAt: Instant,
)

@JvmInline
@Serializable
value class HostId(override val value: Uuid) : TableId<Uuid>

