package ponder.galaxy.server.io

import kabinet.utils.generateUuidString
import kabinet.utils.lerp
import klutch.utils.toStringId
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
import ponder.galaxy.model.data.GalaxyProbe
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLog
import ponder.galaxy.model.data.StarLogId
import ponder.galaxy.model.reddit.ListingType
import ponder.galaxy.model.reddit.REDDIT_URL_BASE
import ponder.galaxy.model.reddit.RedditClient
import ponder.galaxy.model.reddit.RedditLinkDto
import ponder.galaxy.server.db.services.GalaxyTableDao
import ponder.galaxy.server.db.services.StarLogTableDao
import ponder.galaxy.server.db.services.StarTableDao
import kotlin.math.exp
import kotlin.math.max
import kotlin.time.Duration.Companion.minutes

class RedditMonitor(
    private val client: RedditClient,
    private val starDao: StarTableDao = StarTableDao(),
    private val starLogDao: StarLogTableDao = StarLogTableDao(),
    private val galaxyDao: GalaxyTableDao = GalaxyTableDao()
) {

    private var job: Job? = null
    private val subredditNames = listOf("news", "politics", "Artificial", "programming") // "ChatGPT"

    private val _galaxyProbeFlows = mutableMapOf<GalaxyId, MutableStateFlow<GalaxyProbe>>()
    val probeFlows: Map<GalaxyId, StateFlow<GalaxyProbe>> = _galaxyProbeFlows

    fun start() {
        job = CoroutineScope(Dispatchers.IO).launch {

            subredditNames.forEach { subredditName ->
                val galaxy = galaxyDao.readByNameOrInsert(subredditName) {
                    Galaxy(
                        galaxyId = GalaxyId(generateUuidString()),
                        name = subredditName,
                        url = "$REDDIT_URL_BASE/r/$subredditName",
                        visibility = 0f
                    )
                }

                _galaxyProbeFlows[galaxy.galaxyId] = MutableStateFlow(GalaxyProbe(galaxy.galaxyId, emptyList()))
            }

            while(isActive) {

                subredditNames.forEach { subredditName ->
                    val now = Clock.System.now()
                    val starLogs = mutableListOf<StarLog>()

                    val galaxy = galaxyDao.readByName(subredditName) ?: error("galaxy not found: ${subredditName}")
                    val links = client.getListing(subredditName, ListingType.Hot)
                    val visibilitySum = links.sumOf { it.deriveVisibility().toDouble() }.toFloat()
                    val currentVisibility = visibilitySum / links.size
                    val prevVisibility = galaxy.visibility.takeIf { it > 0 } ?: currentVisibility
                    val galaxyVisibility = lerp(prevVisibility, currentVisibility, .1f)

                    galaxyDao.update(galaxy.copy(
                        visibility = galaxyVisibility
                    ))

                    links.forEachIndexed { position, link ->

                        val visibility = link.deriveVisibility()
                        val visibilityRatio = galaxyVisibility.takeIf{ it > 0 }?.let { visibility / it } ?: 0f
                        val createdAt = Instant.fromEpochSeconds(link.createdUtc.toLong())

                        val thumbnail = link.preview?.images?.firstOrNull()?.source?.url

                        val starId = starDao.updateByUrlOrInsert(link.url) {
                            Star(
                                starId = StarId(generateUuidString()),
                                galaxyId = galaxy.galaxyId,
                                title = link.title,
                                link = link.url,
                                permalink = "https://www.reddit.com${link.permalink}",
                                thumbnailUrl = thumbnail,
                                visibility = visibility,
                                voteCount = link.ups,
                                commentCount = link.numComments,
                                updatedAt = now,
                                createdAt = createdAt,
                                discoveredAt = now
                            )
                        }.let { StarId(it.toStringId()) }

                        val age = max(((now - createdAt).inWholeMinutes / (60 * 24)).toFloat(), 10f)
                        val rise = visibilityRatio * exp(-(age * age))

                        val starLogId = starLogDao.insert(StarLog(
                            starLogId = StarLogId(0L),
                            starId = starId,
                            visibility = visibility,
                            rise = rise,
                            commentCount = link.numComments,
                            voteCount = link.ups,
                            createdAt = now,
                        )).let { StarLogId(it) }
                        val starLog = starLogDao.readById(starLogId)
                        starLogs.add(starLog)
                    }

                    _galaxyProbeFlows.getValue(galaxy.galaxyId).value = GalaxyProbe(galaxy.galaxyId, starLogs)
                }
                delay(1.minutes)
            }
        }
    }
}

fun RedditLinkDto.deriveVisibility() = (numComments * 2 + score).toFloat()

