package ponder.galaxy.app.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ponder.galaxy.app.io.SnippetApiClient
import ponder.galaxy.model.data.Snippet
import ponder.galaxy.model.data.UniverseTest
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel

class UniverseTestModel(
    private val snippetApiClient: SnippetApiClient = SnippetApiClient()
): StateModel<UniverseTestState>() {
    override val state = ModelState(UniverseTestState())

    fun testUniverse() {
        viewModelScope.launch(Dispatchers.IO) {
            val tests = snippetApiClient.testUniverse(stateNow.universe) ?: error("Universe tests not found")
            setState { it.copy(tests = tests) }
        }
    }

    fun setUniverse(universe: String) = setState { it.copy(universe = universe) }
}

data class UniverseTestState(
    val universe: String = "My sister was bit by a moose once.",
    val tests: List<UniverseTest> = emptyList(),
)