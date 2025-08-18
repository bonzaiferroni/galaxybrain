@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.model.data

import kabinet.db.TableId
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Snippet(
    val snippetId: SnippetId,
    val text: String,
)

@JvmInline @Serializable
value class SnippetId(override val value: Uuid): TableId<Uuid> {
    companion object {
        fun random() = SnippetId(Uuid.random())
    }
}