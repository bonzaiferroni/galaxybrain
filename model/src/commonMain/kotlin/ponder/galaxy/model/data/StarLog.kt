package ponder.galaxy.model.data

import kabinet.db.TableId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.math.exp
import kotlin.math.max

@Serializable
data class StarLog(
    val starLogId: StarLogId,
    val starId: StarId,
    val visibility: Float,
    val visibilityRatio: Float,
    val commentCount: Int,
    val voteCount: Int,
    val createdAt: Instant,
) {
    fun getRise(startedAt: Instant, riseFactor: Int): Float {
        val age = max((createdAt - startedAt).inWholeMinutes, 10) / (60 * 24).toFloat()
        return visibilityRatio * exp(-riseFactor * (age * age))
    }
}

@JvmInline @Serializable
value class StarLogId(override val value: Long): TableId<Long>