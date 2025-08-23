package ponder.galaxy.model.data

import kabinet.db.TableId
import kabinet.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class SignalScan(
    val signalScanId: SignalScanId,
    val galaxyId: GalaxyId,
    val signalSum: Float,
    val signalCount: Float,
    val createdAt: Instant,
)

@JvmInline @Serializable
value class SignalScanId(override val value: String): TableId<String> {
    companion object {
        fun random() = SignalScanId(randomUuidString())
    }
}