package ponder.galaxy.model.data

import kotlinx.serialization.Serializable

@Serializable
data class SnippetEmbedding(
    val snippetId: SnippetId,
    val vector: FloatArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SnippetEmbedding

        if (snippetId != other.snippetId) return false
        if (!vector.contentEquals(other.vector)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = snippetId.hashCode()
        result = 31 * result + vector.contentHashCode()
        return result
    }
}