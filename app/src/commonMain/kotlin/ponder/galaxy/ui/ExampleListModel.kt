package ponder.galaxy.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.galaxy.io.ExampleStore
import ponder.galaxy.model.data.Example
import ponder.galaxy.model.data.NewExample
import pondui.ui.core.StateModel
import pondui.ui.core.ModelState

class ExampleListModel(
    private val store: ExampleStore = ExampleStore(),
): StateModel<ExampleListState>() {
    override val state = ModelState(ExampleListState())

    init {
        refreshItems()
    }

    fun refreshItems() {
        viewModelScope.launch {
            val examples = store.readUserExamples() ?: error("examples not found")
            setState { it.copy(examples = examples) }
        }
    }

    fun createNewItem() {
        if (!stateNow.isValidNewItem) return
        viewModelScope.launch {
            val exampleId = store.createExample(NewExample(
                label = stateNow.newSymtrix
            )) ?: error("could not create example")
            if (exampleId > 0) {
                refreshItems()
                setState { it.copy(newSymtrix = "") }
            }
        }
    }

    fun deleteItem(example: Example) {
        viewModelScope.launch {
            val isSuccess = store.deleteExample(example.id) ?: error("could not delete example")
            if (isSuccess) {
                refreshItems()
            }
        }
    }

    fun setSymtrix(symtrix: String) {
        setState { it.copy(newSymtrix = symtrix) }
    }
}

data class ExampleListState(
    val examples: List<Example> = emptyList(),
    val newSymtrix: String = "",
) {
    val isValidNewItem get() = newSymtrix.isNotBlank()
}