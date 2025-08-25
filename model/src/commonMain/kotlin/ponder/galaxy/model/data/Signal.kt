package ponder.galaxy.model.data

import kabinet.db.TableId
import kabinet.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Signal(
    val signalId: SignalId,
    val starSnippetId: StarSnippetId,
    val signalScanId: SignalScanId,
    val distance: Float,
    val visibility: Float,
    val createdAt: Instant,
) {
    val value get() = visibility * distance
}

@JvmInline @Serializable
value class SignalId(override val value: String): TableId<String> {
    companion object {
        fun random() = SignalId(randomUuidString())
    }
}