package ponder.galaxy.app.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.galaxy.app.io.QuestionApiClient
import ponder.galaxy.app.io.UniverseApiClient
import ponder.galaxy.model.data.NewUniverse
import ponder.galaxy.model.data.Question
import ponder.galaxy.model.data.QuestionId
import ponder.galaxy.model.data.Universe
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel

class QuestionProfileModel(
    private val questionId: QuestionId,
    private val questionClient: QuestionApiClient = QuestionApiClient(),
    private val universeClient: UniverseApiClient = UniverseApiClient(),
) : StateModel<QuestionProfileState>() {
    override val state = ModelState(QuestionProfileState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val question = questionClient.readById(questionId) ?: error("Question not found")
            val universes = universeClient.readByQuestion(questionId) ?: emptyList()
            setState { it.copy(question = question, universes = universes) }
        }
    }

    fun createUniverse() {
        if (stateNow.newDefinition.isBlank()) return
        val question = stateNow.question ?: return
        viewModelScope.launch {
            val ok = universeClient.create(
                NewUniverse(
                    questionId = question.questionId,
                    definition = stateNow.newDefinition.trim(),
                )
            ) == true
            println(ok)
            if (ok) {
                refresh()
                setState { it.copy(newDefinition = "") }
            }
        }
    }

    fun setUniverseText(text: String) = setState { it.copy(newDefinition = text) }
}

data class QuestionProfileState(
    val question: Question? = null,
    val universes: List<Universe> = emptyList(),
    val newDefinition: String = "",
)
