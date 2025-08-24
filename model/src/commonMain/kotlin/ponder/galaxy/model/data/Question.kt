package ponder.galaxy.model.data

import kabinet.db.TableId
import kabinet.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Question(
    val questionId: QuestionId,
    val text: String,
    val createdAt: Instant
)

@JvmInline @Serializable
value class QuestionId(override val value: String): TableId<String> {
    companion object {
        fun random() = QuestionId(randomUuidString())
    }
}

@Serializable
data class NewQuestion(
    val text: String
)