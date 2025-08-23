package ponder.galaxy.model.data

import kabinet.db.TableId
import kabinet.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Universe(
    val universeId: UniverseId,
    val definition: String,
    val interval: Int, // signal check interval in minutes
    val resolution: Float,
    val createdAt: Instant
)

@JvmInline @Serializable
value class UniverseId(override val value: String): TableId<String> {
    companion object {
        fun random() = UniverseId(randomUuidString())
    }
}