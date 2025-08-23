package ponder.galaxy.model.data

import kabinet.db.TableId
import kabinet.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Star(
    val starId: StarId,
    val galaxyId: GalaxyId,
    val identifier: String?,
    val title: String?,
    val link: String?,
    val url: String,
    val thumbUrl: String?,
    val imageUrl: String?,
    val visibility: Float?,
    val commentCount: Int?,
    val voteCount: Int?,
    val wordCount: Int?,
    val accessedAt: Instant?,
    val publishedAt: Instant?,
    val updatedAt: Instant,
    val createdAt: Instant,
) {
    val displayTitle get() = title ?: "[Title missing]"
    val existedAt get() = publishedAt ?: createdAt
}

@JvmInline @Serializable
value class StarId(override val value: String): TableId<String> {

    companion object {
        fun random() = StarId(randomUuidString())
    }
}
