package ponder.galaxy.model.data

import kabinet.db.TableId
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Example(
    val id: Long,
    val userId: Long,
    val label: String,
)

@JvmInline
@Serializable
value class ExampleId(override val value: Long): TableId<Long>