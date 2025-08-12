package ponder.galaxy.ui

import androidx.lifecycle.viewModelScope
import kabinet.model.SpeechRequest
import kabinet.model.SpeechVoice
import kabinet.utils.toAgoDescription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.galaxy.globalProbeService
import ponder.galaxy.io.GalaxySource
import ponder.galaxy.io.IdeaApiClient
import ponder.galaxy.io.ProbeService
import ponder.galaxy.model.Api
import ponder.galaxy.model.data.Galaxy
import ponder.galaxy.model.data.Idea
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import pondui.io.GeminiApiClient
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel
import kotlin.collections.remove
import kotlin.rem
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class IdeaFeedModel(
    private val probeService: ProbeService = globalProbeService,
    private val galaxySource: GalaxySource = GalaxySource(),
    // private val geminiClient: GeminiApiClient = GeminiApiClient(Api.Gemini)
    private val ideaClient: IdeaApiClient = IdeaApiClient()
): StateModel<IdeaFeedState>() {
    override val state = ModelState(IdeaFeedState())

    private val generatedSpeech = mutableMapOf<StarId, Instant>()
    private val queuedSpeech = mutableListOf<Star>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val galaxies = galaxySource.readAll()

            launch {
                probeService.stateFlow.collect { state ->
                    takeStars(state.stars, galaxies)
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
        for (galaxy in galaxies) {
            val star = stars.firstOrNull { it.galaxyId == galaxy.galaxyId } ?: continue
            if (generatedSpeech.contains(star.starId)) continue
            val now = Clock.System.now()
            generatedSpeech[star.starId] = now
            println("queuing speech: ${galaxy.name}")

            queuedSpeech.add(star)
        }
    }

    private suspend fun generateSpeech(star: Star, galaxy: Galaxy) {
        val idea = ideaClient.readIdeasByStarId(star.starId).firstOrNull() ?: return
        setState { it.copy(idea = idea, star = star, galaxy = galaxy)}
    }
}

data class IdeaFeedState(
    val star: Star? = null,
    val idea: Idea? = null,
    val galaxy: Galaxy? = null,
)