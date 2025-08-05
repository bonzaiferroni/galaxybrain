package ponder.galaxy.server.io

import kabinet.utils.generateUuidString
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
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class RedditMonitor(
    private val client: RedditClient,
    private val starDao: StarTableDao = StarTableDao(),
    private val starLogDao: StarLogTableDao = StarLogTableDao(),
    private val galaxyDao: GalaxyTableDao = GalaxyTableDao()
) {

    private var job: Job? = null
    private val subredditNames = listOf("news")

    private val _starLogFlow = MutableStateFlow<List<StarLog>>(emptyList())
    val starLogFlow: StateFlow<List<StarLog>> = _starLogFlow

    fun start() {
        job = CoroutineScope(Dispatchers.IO).launch {

            while(isActive) {
                val starLogs = mutableListOf<StarLog>()
                val now = Clock.System.now()
                subredditNames.forEach { subredditName ->

                    val links = client.getListing(subredditName, ListingType.Hot)
                    val visibilitySum = links.sumOf { it.deriveVisibility().toDouble() }.toFloat()
                    val galaxyVisibility = visibilitySum / links.size

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
                        val visibilityRatio = galaxyVisibility.takeIf{ it > 0 }?.let { visibility / galaxyVisibility } ?: 1f
                        val createdAt = Instant.fromEpochSeconds(link.createdUtc.toLong())

                        val starId = starDao.updateByUrlOrInsert(link.url) {
                            Star(
                                starId = StarId(generateUuidString()),
                                galaxyId = galaxy.galaxyId,
                                title = link.title,
                                url = link.url,
                                visibility = visibility,
                                voteCount = link.ups,
                                commentCount = link.numComments,
                                updatedAt = now,
                                createdAt = createdAt,
                                discoveredAt = now
                            )
                        }.let { StarId(it.toStringId()) }

                        val age = (now - createdAt).inWholeMinutes / (60 * 24).toFloat()
                        val rise = visibilityRatio / age

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
                }
                _starLogFlow.value = starLogs
                println("added ${starLogs.size} stars")
                delay(1.minutes)
            }
        }
    }
}

fun RedditLinkDto.deriveVisibility() = (numComments * 2 + ups).toFloat()

