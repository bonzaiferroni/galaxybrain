package ponder.galaxy.server.io

import kabinet.utils.format
import kabinet.utils.generateUuidString
import kabinet.utils.lerp
import klutch.utils.toStringId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
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
import kotlin.math.pow
import kotlin.time.Duration.Companion.minutes

class RedditMonitor(
    private val client: RedditClient,
    private val starDao: StarTableDao = StarTableDao(),
    private val starLogDao: StarLogTableDao = StarLogTableDao(),
    private val galaxyDao: GalaxyTableDao = GalaxyTableDao()
) {

    private var job: Job? = null
    private val subredditNames = listOf("news", "politics", "Artificial", "programming") // "ChatGPT"

    private val _galaxyProbeFlow = MutableSharedFlow<GalaxyProbe>(replay = 1)
    val galaxyProbeFlow: SharedFlow<GalaxyProbe> = _galaxyProbeFlow

    fun start() {
        job = CoroutineScope(Dispatchers.IO).launch {

            while(isActive) {
                subredditNames.forEach { subredditName ->
                    val now = Clock.System.now()
                    val starLogs = mutableListOf<StarLog>()

                    val prevVisibility = galaxyDao.readByName(subredditName)?.visibility
                    val links = client.getListing(subredditName, ListingType.Hot)
                    val visibilitySum = links.sumOf { it.deriveVisibility().toDouble() }.toFloat()
                    val currentVisibility = visibilitySum / links.size
                    val galaxyVisibility = lerp(prevVisibility ?: currentVisibility, currentVisibility, .1f)

                    val galaxy = galaxyDao.readByNameOrInsert(subredditName) {
                        Galaxy(
                            galaxyId = GalaxyId(generateUuidString()),
                            name = subredditName,
                            url = "$REDDIT_URL_BASE/r/$subredditName",
                            visibility = galaxyVisibility
                        )
                    }

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
                    _galaxyProbeFlow.emit(GalaxyProbe(galaxy.galaxyId, starLogs))
                }
                delay(1.minutes)
            }
        }
    }
}

fun RedditLinkDto.deriveVisibility() = (numComments * 2 + score).toFloat()

