package ponder.galaxy.app.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ponder.galaxy.app.appApi
import ponder.galaxy.app.io.ApiClients
import ponder.galaxy.model.data.NewUniverse
import ponder.galaxy.model.data.Question
import ponder.galaxy.model.data.QuestionId
import ponder.galaxy.model.data.SignalScan
import ponder.galaxy.model.data.Universe
import ponder.galaxy.model.data.UniverseId
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel

class QuestionProfileModel(
    private val questionId: QuestionId,
    private val api: ApiClients = appApi
) : StateModel<QuestionProfileState>() {
    override val state = ModelState(QuestionProfileState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val questionJob = async { api.question.readById(questionId) ?: error("Question not found") }
            val universeJob = async { api.universe.readByQuestion(questionId) ?: emptyList() }
            val universes = universeJob.await()
            val scanJobs = universes.map { it.universeId to async { api.universe.readScansByUniverseId(it.universeId) } }
            val scans = scanJobs.associateBy({ it.first }, { it.second.await() ?: emptyList() })
            val question = questionJob.await()
            withContext(Dispatchers.Main) {
                setState { it.copy(question = question, universes = universes, scans = scans) }
            }
        }
    }

    fun createUniverse() {
        if (stateNow.newDefinition.isBlank()) return
        val question = stateNow.question ?: return
        viewModelScope.launch {
            val ok = api.universe.create(
                NewUniverse(
                    questionId = question.questionId,
                    definition = stateNow.newDefinition.trim(),
                )
            ) == true
            if (ok) {
                setState { it.copy(newDefinition = "") }
                refresh()
            }
        }
    }

    fun setUniverseText(text: String) = setState { it.copy(newDefinition = text) }
}

data class QuestionProfileState(
    val question: Question? = null,
    val universes: List<Universe> = emptyList(),
    val scans: Map<UniverseId, List<SignalScan>> = emptyMap(),
    val newDefinition: String = "",
)
