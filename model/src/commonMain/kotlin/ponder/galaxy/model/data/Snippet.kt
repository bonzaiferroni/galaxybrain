package ponder.galaxy.model.data

import kabinet.db.TableId
import kabinet.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Snippet(
    val snippetId: SnippetId,
    val text: String,
)

@JvmInline @Serializable
value class SnippetId(override val value: String): TableId<String> {
    companion object {
        fun random() = SnippetId(randomUuidString())
    }
}