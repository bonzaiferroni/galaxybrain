package ponder.galaxy.app.ui

import androidx.lifecycle.viewModelScope
import kabinet.utils.startOfDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.galaxy.app.globalProbeService
import ponder.galaxy.app.io.GalaxyApiClient
import ponder.galaxy.app.io.IdeaApiClient
import ponder.galaxy.app.io.ProbeService
import ponder.galaxy.model.data.Galaxy
import ponder.galaxy.model.data.Idea
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class IdeaFocusModel(
    private val probeService: ProbeService = globalProbeService,
    private val galaxyApiClient: GalaxyApiClient = GalaxyApiClient(),
    // private val geminiClient: GeminiApiClient = GeminiApiClient(Api.Gemini)
    private val ideaClient: IdeaApiClient = IdeaApiClient()
): StateModel<IdeaFocusState>() {
    override val state = ModelState(IdeaFocusState())

    companion object {
        private val generatedSpeech = mutableMapOf<StarId, Instant>()
    }

    private val queuedSpeech = mutableListOf<Star>()

    init {
        val startOfDay = Clock.startOfDay()
        viewModelScope.launch(Dispatchers.IO) {
            val galaxies = galaxyApiClient.readAll() ?: error("galaxies not found")
            val ideas = ideaClient.readIdeas(Clock.startOfDay()) ?: error("ideas not found")
            for (idea in ideas) {
                val starId = idea.starId ?: continue
                generatedSpeech[starId] = idea.createdAt
            }

            launch {
                probeService.stateFlow.collect { state ->
                    takeStars(
                        stars = state.stars.filter { it.createdAt > startOfDay },
                        galaxies = galaxies
                    )
                }
            }

            launch {
                delay(10.seconds)
                while (isActive) {
                    val star = queuedSpeech.firstOrNull()
                    if (star != null) {
                        generateSpeech(star, galaxies.first { it.galaxyId == star.galaxyId })
                        queuedSpeech.remove(star)
                    }
                    delay(1.minutes)
                }
            }
        }
    }

    private fun takeStars(stars: List<Star>, galaxies: List<Galaxy>) {
        println("looking through ${stars.size} stars")
        for (galaxy in galaxies) {
            val star = stars.firstOrNull { it.galaxyId == galaxy.galaxyId } ?: continue
            if (star.visibility == null) continue
            if (generatedSpeech.contains(star.starId)) continue
            val now = Clock.System.now()
            generatedSpeech[star.starId] = now
            println("queuing speech: ${galaxy.name} ${star.starId}")

            queuedSpeech.add(star)
        }
    }

    private suspend fun generateSpeech(star: Star, galaxy: Galaxy) {
        val idea = ideaClient.readHeadlineIdea(star.starId, true)
        setState { it.copy(idea = idea, star = star, galaxy = galaxy)}
    }
}

data class IdeaFocusState(
    val star: Star? = null,
    val idea: Idea? = null,
    val galaxy: Galaxy? = null,
)