package ponder.galaxy.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ponder.galaxy.globalProbeService
import ponder.galaxy.io.GalaxyApiClient
import ponder.galaxy.io.IdeaApiClient
import ponder.galaxy.io.ProbeService
import ponder.galaxy.io.SnippetApiClient
import ponder.galaxy.io.StarApiClient
import ponder.galaxy.io.StarLinkApiClient
import ponder.galaxy.model.data.Galaxy
import ponder.galaxy.model.data.Idea
import ponder.galaxy.model.data.Snippet
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLink
import ponder.galaxy.model.data.StarLog
import pondui.LocalValueSource
import pondui.ui.core.ModelState
import pondui.ui.core.StateModel

class StarProfileModel(
    private val starId: StarId,
    private val probeService: ProbeService = globalProbeService,
    private val galaxyApiClient: GalaxyApiClient = GalaxyApiClient(),
    private val valueSource: LocalValueSource = LocalValueSource(),
    private val ideaApiClient: IdeaApiClient = IdeaApiClient(),
    private val starApiClient: StarApiClient = StarApiClient(),
    private val starLinkApiClient: StarLinkApiClient = StarLinkApiClient(),
    private val snippetApiClient: SnippetApiClient = SnippetApiClient(),
): StateModel<StarProfileState>() {

    override val state = ModelState(StarProfileState())

    init {
        viewModelScope.launch {
            val star = probeService.getStar(starId) ?: starApiClient.readById(starId) ?: return@launch
            val starLogs = probeService.readStarLogs(starId) ?: emptyList()
            val riseFactor = valueSource.readInt(RISE_FACTOR_KEY, 1)
            val galaxy = galaxyApiClient.readGalaxyById(star.galaxyId)
            val idea = ideaApiClient.readContentIdea(starId, false)
            val outgoingLinks = starLinkApiClient.readOutgoingLinks(starId) ?: emptyList()
            val snippets = snippetApiClient.readStarSnippets(starId) ?: emptyList()

            val linkStar: Star? = star.link?.let { starApiClient.readByUrl(it) }

            setState { it.copy(
                star = star,
                linkStar = linkStar,
                starLogs = starLogs,
                riseFactor = riseFactor,
                galaxy = galaxy,
                contentIdea = idea,
                outgoingLinks = outgoingLinks,
                snippets = snippets,
            ) }

            probeService.stateFlow.collect { state ->
                val starLogs = state.starLogs[starId] ?: return@collect
                setState { it.copy(starLogs = starLogs)}
            }
        }
    }

    fun generateAudio() {
        viewModelScope.launch(Dispatchers.IO) {
            val idea = ideaApiClient.readContentIdea(starId, true) ?: return@launch
            withContext(Dispatchers.Main) {
                setState { it.copy(contentIdea = idea, isPlaying = true) }
            }
        }
    }

    fun toggleIsPlaying(value: Boolean = !stateNow.isPlaying) {
        setState { it.copy(isPlaying = value)}
    }

    fun discoverLink() {
        val link = stateNow.star?.link ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val star = starApiClient.readByUrl(link, true)
            setState { it.copy(linkStar = star)}
        }
    }
}

@Stable
data class StarProfileState(
    val star: Star? = null,
    val linkStar: Star? = null,
    val galaxy: Galaxy? = null,
    val starLogs: List<StarLog> = emptyList(),
    val riseFactor: Int = 1,
    val contentIdea: Idea? = null,
    val isPlaying: Boolean = false,
    val outgoingLinks: List<StarLink> = emptyList(),
    val snippets: List<Snippet> = emptyList(),
)