package ponder.galaxy.app.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ponder.galaxy.app.QuestionProfileRoute
import pondui.ui.controls.Button
import pondui.ui.controls.LazyScaffold
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.controls.actionable

// Ahoy! Rustbeard presents the Question Feed, a scrollin' ledger o' queries, yarrr!
@Composable
fun QuestionFeedScreen(
    viewModel: QuestionFeedModel = viewModel { QuestionFeedModel() }
) {
    val state by viewModel.stateFlow.collectAsState()

    LazyScaffold {
        item("QuestionCreate") {
            Row(1) {
                TextField(
                    text = state.questionText,
                    label = "Question",
                    placeholder = "What be on yer mind?",
                    onTextChanged = viewModel::setQuestionText,
                    modifier = Modifier.weight(1f)
                )
                Button(text = "Create", onClick = viewModel::createQuestion)
            }
        }
        items(state.questions) { question ->
            Text(
                text = question.text,
                modifier = Modifier.actionable(QuestionProfileRoute(question.questionId.value))
            )
        }
    }
}
