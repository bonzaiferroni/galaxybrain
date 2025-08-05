package ponder.galaxy.model.data

import kabinet.db.TableId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class StarLog(
    val starLogId: StarLogId,
    val starId: StarId,
    val visibility: Float,
    val createdAt: Instant,
)

@JvmInline @Serializable
value class StarLogId(override val value: Long): TableId<Long>