package ponder.galaxy.app.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ponder.galaxy.app.io.QuestionApiClient
import ponder.galaxy.model.data.NewQuestion
import ponder.galaxy.model.data.Question
import ponder.galaxy.model.data.QuestionId
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel

// Avast! Rustbeard be riggin' a client to haul in questions from the horizon, yarrr!
class QuestionFeedModel(
    private val questionClient: QuestionApiClient = QuestionApiClient()
) : StateModel<QuestionFeedState>() {
    override val state = ModelState(QuestionFeedState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val questions = questionClient.readAll() ?: emptyList()
            setState { it.copy(questions = questions) }
        }
    }

    fun createQuestion() {
        if (stateNow.questionText.isBlank()) return
        viewModelScope.launch {
            val ok = questionClient.create(
                NewQuestion(
                    text = stateNow.questionText.trim(),
                )
            ) == true
            if (ok) {
                refresh()
                setState { it.copy(questionText = "") }
            }
        }
    }

    fun setQuestionText(text: String) = setState { it.copy(questionText = text) }
}

// The state be holdin' a haul o' questions, ready to be read.
data class QuestionFeedState(
    val questionText: String = "",
    val questions: List<Question> = emptyList()
)
