@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.io

import kabinet.console.LogColor
import kabinet.console.LogJustify
import kabinet.console.globalConsole
import kabinet.utils.lerp
import kabinet.utils.toMetricString
import kabinet.web.Url
import kabinet.web.fromHref
import kabinet.web.fromHrefOrNull
import klutch.utils.toStringId
import klutch.web.RedditReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.galaxy.model.data.Galaxy
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.GalaxyProbe
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLink
import ponder.galaxy.model.data.StarLinkId
import ponder.galaxy.model.data.StarLog
import ponder.galaxy.model.data.StarLogId
import ponder.galaxy.model.reddit.ListingType
import ponder.galaxy.model.reddit.REDDIT_URL_BASE
import ponder.galaxy.model.reddit.RedditClient
import ponder.galaxy.model.reddit.RedditArticleDto
import ponder.galaxy.server.db.services.GalaxyTableDao
import ponder.galaxy.server.db.services.HostTableService
import ponder.galaxy.server.db.services.SnippetTableService
import ponder.galaxy.server.db.services.StarLinkTableDao
import ponder.galaxy.server.db.services.StarLogTableDao
import ponder.galaxy.server.db.services.StarTableDao
import kotlin.math.min
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.ExperimentalUuidApi

private val console = globalConsole.getHandle(RedditMonitor::class)

class RedditMonitor(
    private val client: RedditClient,
    private val starDao: StarTableDao = StarTableDao(),
    private val starLogDao: StarLogTableDao = StarLogTableDao(),
    private val starLinkDao: StarLinkTableDao = StarLinkTableDao(),
    private val galaxyDao: GalaxyTableDao = GalaxyTableDao(),
    private val hostService: HostTableService = HostTableService(),
    private val snippetService: SnippetTableService = SnippetTableService(),
    private val redditReader: RedditReader = RedditReader()
) {

    private var job: Job? = null
    private val subredditNames = listOf(
        "news", "politics", "worldnews",
        "Artificial", "science", "technology", "futurology",
        "dataisbeautiful", "InternetIsBeautiful", "whatisthisbug", "outoftheloop", "philosophy", "MadeMeSmile",
        "gnome", "linuxmasterrace", "opensource", "linux",
        "programming", "Kotlin", "androiddev", "redditdev", "webdev", "programmerhumor",
    ) //

    private val _galaxyProbeFlows = mutableMapOf<GalaxyId, MutableStateFlow<GalaxyProbe>>()
    private val lock = Mutex()
    suspend fun getFlows(): List<StateFlow<GalaxyProbe>> = lock.withLock { _galaxyProbeFlows.values.toList() }
    private suspend fun initializeFlow(galaxyId: GalaxyId, flow: MutableStateFlow<GalaxyProbe>) =
        lock.withLock { _galaxyProbeFlows[galaxyId] = flow }

    private suspend fun setFlowState(galaxyId: GalaxyId, probe: GalaxyProbe) =
        lock.withLock { _galaxyProbeFlows[galaxyId]?.value = probe }

    fun start() {
        job = CoroutineScope(Dispatchers.IO).launch {

            val redditUrl = Url.fromHref("https://reddit.com")
            val host = hostService.dao.readByUrl(redditUrl) ?: hostService.createByUrl(redditUrl)

            subredditNames.forEach { subredditName ->
                launch {
                    val galaxy = galaxyDao.readByNameOrInsert(subredditName) {
                        Galaxy(
                            galaxyId = GalaxyId.random(),
                            hostId = host.hostId,
                            name = subredditName,
                            url = "$REDDIT_URL_BASE/r/$subredditName",
                            visibility = 0f,
                            createdAt = Clock.System.now(),
                        )
                    }
                    // val starLogs = galaxyDao.readLatestStarLogs(galaxy.galaxyId)
                    initializeFlow(galaxy.galaxyId, MutableStateFlow(GalaxyProbe(galaxy.galaxyId, emptyList())))

                    while (isActive) {
                        val now = Clock.System.now()
                        val starLogs = mutableListOf<StarLog>()

                        val galaxy = galaxyDao.readByName(subredditName) ?: error("galaxy not found: $subredditName")
                        val articles = client.getArticles(subredditName, ListingType.Hot)
                        if (articles == null) {
                            console.logError("RedditMonitor: articles not found")
                            delay(1.minutes)
                            continue
                        }
                        val visibilitySum = articles.sumOf { it.deriveVisibility().toDouble() }.toFloat()
                        val currentVisibility = visibilitySum / articles.size
                        val prevVisibility = galaxy.visibility.takeIf { it > 0 } ?: currentVisibility
                        val galaxyVisibility = lerp(prevVisibility, currentVisibility, .1f)

                        galaxyDao.update(
                            galaxy.copy(
                                visibility = galaxyVisibility
                            )
                        )

                        articles.forEach { article ->

                            val visibility = article.deriveVisibility()
                            val visibilityRatio = galaxyVisibility.takeIf { it > 0 }?.let { visibility / it } ?: 0f
                            val publishedAt = Instant.fromEpochSeconds(article.createdUtc.toLong())

                            val thumbUrl = article.preview?.images?.minBy { it.source.width }?.source?.url
                            val imageUrl = article.preview?.images?.maxBy { it.source.width }?.source?.url
                            val starUrl =
                                Url.fromHrefOrNull("https://www.reddit.com${article.permalink}") ?: return@forEach

                            val document = article.selftext.takeIf { it.isNotEmpty() }?.let {
                                redditReader.read(
                                    title = article.title,
                                    url = starUrl,
                                    text = it
                                )
                            }

                            val starId = starDao.updateByUrlOrInsert(starUrl.href, galaxy.galaxyId) {
                                Star(
                                    starId = StarId.random(),
                                    galaxyId = galaxy.galaxyId,
                                    identifier = article.id,
                                    title = article.title,
                                    link = article.url.takeIf { link -> redditHosts.none { link.contains(it) } },
                                    url = starUrl.href,
                                    thumbUrl = thumbUrl,
                                    imageUrl = imageUrl,
                                    visibility = visibility,
                                    voteCount = article.ups,
                                    wordCount = document?.wordCount,
                                    commentCount = article.numComments,
                                    publishedAt = publishedAt,
                                    accessedAt = now,
                                    updatedAt = now,
                                    createdAt = now,
                                )
                            }.let { StarId(it.toStringId()) }

                            article.url.takeIf { it.isNotEmpty() && !it.contains("reddit.com") && !it.contains("redd.it") }
                                ?.let { href ->
                                    val url = Url.fromHrefOrNull(href) ?: return@let
                                    val starLink = starLinkDao.readByUrl(url)
                                    if (starLink != null) return@let
                                    val toStar = starDao.readByUrl(url)
                                    starLinkDao.insert(
                                        StarLink(
                                            starLinkId = StarLinkId.random(),
                                            fromStarId = starId,
                                            toStarId = toStar?.starId,
                                            snippetId = null,
                                            commentId = null,
                                            url = url,
                                            text = null,
                                            startIndex = null,
                                            createdAt = now,
                                        )
                                    )
                                }

                            document?.let {
                                snippetService.createOrUpdateStarSnippets(starId, document)
                            }

                            val starLogId = starLogDao.insert(
                                StarLog(
                                    starLogId = StarLogId(0L),
                                    starId = starId,
                                    visibility = visibility,
                                    visibilityRatio = visibilityRatio,
                                    commentCount = article.numComments,
                                    voteCount = article.ups,
                                    createdAt = now,
                                )
                            ).let { StarLogId(it) }
                            val starLog = starLogDao.readById(starLogId)
                            starLogs.add(starLog)
                        }

                        setFlowState(galaxy.galaxyId, GalaxyProbe(galaxy.galaxyId, starLogs))

                        val delayMinutes = min(20000 / galaxyVisibility, 10f).toDouble().minutes
                        console.cell(subredditName, 20, justify = LogJustify.LEFT)
                            .cell((delayMinutes.inWholeSeconds / 60f).toMetricString(), 4, LogColor.Blue)
                            .send(background = LogColor.Purple)
                        delay(delayMinutes)
                    }
                }
            }
        }
    }
}

fun RedditArticleDto.deriveVisibility() = (numComments * 2 + score).toFloat()

val redditHosts = listOf(
    "reddit.com",
    "redd.it"
)