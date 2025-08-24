package ponder.galaxy.server.db.services

import kabinet.console.globalConsole
import klutch.db.DbService
import kotlinx.datetime.Clock
import ponder.galaxy.model.data.NewUniverse
import ponder.galaxy.model.data.Universe
import ponder.galaxy.model.data.UniverseId

private val console = globalConsole.getHandle(UniverseTableService::class)

class UniverseTableService(
    val dao: UniverseTableDao = UniverseTableDao(),
    // private val questionDao: QuestionTableDao = QuestionTableDao()
): DbService() {
    suspend fun createUniverse(newUniverse: NewUniverse): Boolean = dbQuery {
        val definition = newUniverse.definition.trim()
        if (definition.isEmpty()) return@dbQuery false
        // val question = questionDao.readByIdOrNull(newUniverse.questionId) ?: error("Question not found")
        val universeCount = dao.countByQuestionId(newUniverse.questionId)

        val label = universeCount.toInt().toGreekLetter() ?: "New Universe"
        val universe = Universe(
            universeId = UniverseId.random(),
            questionId = newUniverse.questionId,
            label = label,
            definition = definition,
            imgUrl = null,
            thumbUrl = null,
            interval = 60 * 24,
            coherence = null,
            signal = null,
            createdAt = Clock.System.now()
        )
        dao.insert(universe)
        console.log("Created universe: $label")
        true
    }
}

private fun Int.toGreekLetter(): String? {
    return greekLetters.getOrNull(this)
}

private val greekLetters = listOf(
    "Alpha", "Beta", "Gamma", "Delta", "Epsilon",
    "Zeta", "Eta", "Theta", "Iota", "Kappa",
    "Lambda", "Mu", "Nu", "Xi", "Omicron",
    "Pi", "Rho", "Sigma", "Tau", "Upsilon",
    "Phi", "Chi", "Psi", "Omega"
)
