package ponder.galaxy.model.data

import kabinet.db.TableId
import kabinet.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Universe(
    val universeId: UniverseId,
    val questionId: QuestionId,
    val label: String,
    val definition: String,
    val imgUrl: String?,
    val thumbUrl: String?,
    val interval: Int, // signal check interval in minutes
    val coherence: Float?,
    val signal: Float?,
    val embedding: FloatArray,
    val createdAt: Instant
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Universe

        if (interval != other.interval) return false
        if (coherence != other.coherence) return false
        if (signal != other.signal) return false
        if (universeId != other.universeId) return false
        if (questionId != other.questionId) return false
        if (label != other.label) return false
        if (definition != other.definition) return false
        if (imgUrl != other.imgUrl) return false
        if (thumbUrl != other.thumbUrl) return false
        if (createdAt != other.createdAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = interval
        result = 31 * result + (coherence?.hashCode() ?: 0)
        result = 31 * result + (signal?.hashCode() ?: 0)
        result = 31 * result + universeId.hashCode()
        result = 31 * result + questionId.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + definition.hashCode()
        result = 31 * result + (imgUrl?.hashCode() ?: 0)
        result = 31 * result + (thumbUrl?.hashCode() ?: 0)
        result = 31 * result + createdAt.hashCode()
        return result
    }
}

@JvmInline @Serializable
value class UniverseId(override val value: String): TableId<String> {
    companion object {
        fun random() = UniverseId(randomUuidString())
    }
}

@Serializable
data class NewUniverse(
    val questionId: QuestionId,
    val definition: String,
)