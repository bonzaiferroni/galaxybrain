package ponder.galaxy.model.data

import kabinet.db.TableId
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Idea(
    val ideaId: IdeaId,
    val audioUrl: String?,
    val text: String?,
    val imageUrl: String?,
    val thumbUrl: String?,
)

@JvmInline
@Serializable
value class IdeaId(override val value: String): TableId<String>