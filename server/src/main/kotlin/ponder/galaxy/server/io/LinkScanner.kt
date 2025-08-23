package ponder.galaxy.server.io

import kabinet.utils.startOfDay
import kabinet.web.Url
import kabinet.web.fromHrefOrNull
import klutch.db.DbService
import klutch.db.read
import klutch.utils.greaterEq
import klutch.utils.toUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.and
import ponder.galaxy.model.data.LinkVisitOutcome
import ponder.galaxy.server.db.services.StarTableService
import ponder.galaxy.server.db.tables.StarTable
import ponder.galaxy.server.db.tables.toStar
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

class LinkScanner(
    private val linkScout: LinkScout = LinkScout(),
    private val starService: StarTableService = StarTableService(),
): DbService() {
    private var job: Job? = null

    fun start() {
        val scannedIds = mutableSetOf<UUID>()
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val originStars = readCreatedAfter(scannedIds, Clock.startOfDay())
                    .sortedByDescending { it.visibility }
                    .mapNotNull { star -> star.link?.let { Url.fromHrefOrNull(it) }?.let {Pair(star, it) }  }
                    .groupBy { it.second.core }
                    .map { it.value.first() }

                val outcomes = mutableMapOf<LinkVisitOutcome, Int>()
                val jobs = mutableListOf<Job>()
                println("scanning ${originStars.size} stars")
                for ((originStar, url) in originStars) {
                    scannedIds.add(originStar.starId.toUUID())

                    val isVisitRecommended = linkScout.isVisitRecommended(url)
                    val star = starService.discoverStarFromUrl(url.href, isVisitRecommended)
                    val outcome = if (isVisitRecommended) {
                        val wordCount = star?.wordCount
                        when {
                            wordCount == null -> LinkVisitOutcome.NotFound
                            wordCount < 50 -> LinkVisitOutcome.OkNoContent
                            else -> LinkVisitOutcome.OkContent
                        }
                    } else {
                        LinkVisitOutcome.Skipped
                    }
                    linkScout.trackVisit(url, outcome)
                    outcomes[outcome] = (outcomes[outcome] ?: 0) + 1
                    delay(1.seconds)
//                    val job = launch {
//
//                    }
//                    jobs.add(job)
                }
                delay((60..90).random() * 1000L)
                jobs.forEach { it.cancel() }
                println("LinkScanner: $outcomes")
            }
        }
    }

    suspend fun readCreatedAfter(scanned: Set<UUID>, time: Instant) = dbQuery {
        val isExplorable = StarTable.createdAt.greaterEq(time) and StarTable.link.isNotNull()
        val tableUrls = StarTable.select(StarTable.url)
        StarTable.read { it.id.notInList(scanned) and isExplorable and StarTable.link.notInSubQuery(tableUrls) }
            .map { it.toStar() }
    }
}