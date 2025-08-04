package ponder.galaxy.server.io

import kabinet.utils.generateUuidString
import kabinet.utils.startOfDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.galaxy.model.data.Galaxy
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.reddit.ListingType
import ponder.galaxy.model.reddit.REDDIT_URL_BASE
import ponder.galaxy.model.reddit.RedditClient
import ponder.galaxy.model.reddit.RedditLinkDto
import ponder.galaxy.server.db.services.GalaxyTableDao
import ponder.galaxy.server.db.services.StarTableDao
import kotlin.time.Duration.Companion.minutes

class RedditMonitor(
    private val client: RedditClient,
    private val starDao: StarTableDao = StarTableDao(),
    private val galaxyDao: GalaxyTableDao = GalaxyTableDao()
) {

    private var job: Job? = null
    private val subredditNames = listOf("news")

    private val _starFlow = MutableStateFlow<List<Star>>(emptyList())
    val starFlow: StateFlow<List<Star>> = _starFlow

    fun start() {
        job = CoroutineScope(Dispatchers.IO).launch {

            val initialStars = starDao.readVisibleStars(20, Clock.startOfDay())
            _starFlow.value = initialStars

            while(isActive) {
                val stars = mutableListOf<Star>()
                val now = Clock.System.now()
                subredditNames.forEach { subredditName ->
                    val galaxy = galaxyDao.readByNameOrInsert(subredditName) {
                        Galaxy(
                            galaxyId = GalaxyId(generateUuidString()),
                            name = subredditName,
                            url = "$REDDIT_URL_BASE/r/$subredditName"
                        )
                    }
                    val links = client.getListing(subredditName, ListingType.Rising)
                    links.forEach { link ->
                        val visibility = link.deriveVisibility()
                        val starId = starDao.updateByUrlOrInsert(link.url) {
                            Star(
                                starId = StarId(generateUuidString()),
                                galaxyId = galaxy.galaxyId,
                                title = link.title,
                                url = link.url,
                                visibility = visibility,
                                updatedAt = now,
                                createdAt = Instant.fromEpochSeconds(link.createdUtc.toLong()),
                                discoveredAt = now
                            )
                        }
                        val star = starDao.readById(starId)
                        stars.add(star)
                    }
                }
                _starFlow.value = stars
                println("added ${stars.size} stars")
                delay(1.minutes)
            }
        }
    }
}

fun RedditLinkDto.deriveVisibility() = (numComments * 2 + ups).toFloat()