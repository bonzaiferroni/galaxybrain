package ponder.galaxy.server.io

import kabinet.web.Url
import klutch.db.DbService
import klutch.db.read
import klutch.utils.eq
import klutch.utils.greaterEq
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import ponder.galaxy.model.data.HostId
import ponder.galaxy.model.data.LinkVisit
import ponder.galaxy.model.data.LinkVisitId
import ponder.galaxy.model.data.LinkVisitOutcome
import ponder.galaxy.server.db.services.HostTableService
import ponder.galaxy.server.db.services.LinkVisitTableDao
import ponder.galaxy.server.db.tables.LinkVisitTable
import ponder.galaxy.server.db.tables.toLinkVisit
import kotlin.math.pow
import kotlin.time.Duration.Companion.days

class LinkScout(
    private val linkVisitDao: LinkVisitTableDao = LinkVisitTableDao(),
    private val hostService: HostTableService = HostTableService(),
): DbService() {

    suspend fun isVisitRecommended(url: Url): Boolean {
        if (url.isFile() || variabilityScore(url.path) < VARIABILITY_MIN_SCORE) return false
        return recommendFromVisits(url)
    }

    suspend fun trackVisit(url: Url, outcome: LinkVisitOutcome) {
        val host = hostService.dao.readByCore(url.core) ?: hostService.createByUrl(url)
        linkVisitDao.insert(LinkVisit(
            linkVisitId = LinkVisitId(0),
            hostId = host.hostId,
            url = url.href,
            outcome = outcome,
            createdAt = Clock.System.now()
        ))
    }

    private suspend fun recommendFromVisits(url: Url): Boolean {
        val host = hostService.dao.readByCore(url.core) ?: hostService.createByUrl(url)
        val visits = readNotSkippedByHost(host.hostId)
        return recommendFromVisits(visits)
    }

    private fun recommendFromVisits(visits: List<LinkVisit>): Boolean {
        val visitMap = visits.groupBy({ it.outcome })
        if (visitMap.getCount(LinkVisitOutcome.NotAllowed) > 0) return false
        if (visitMap.getCount(LinkVisitOutcome.OkContent) > 0) return true
        val noContentCount = visitMap.getCount(LinkVisitOutcome.OkNoContent) + visitMap.getCount(LinkVisitOutcome.NotFound)
        return noContentCount < 10
    }

    suspend fun readNotSkippedByHost(
        hostId: HostId,
        after: Instant = Clock.System.now() - 3.days,
        limit: Int = 100
    ) = dbQuery {
        LinkVisitTable.read { it.hostId.eq(hostId) and it.createdAt.greaterEq(after) and it.outcome.neq(LinkVisitOutcome.Skipped) }
            .orderBy(LinkVisitTable.createdAt, SortOrder.DESC)
            .limit(limit)
            .map { it.toLinkVisit() }
    }
}

private fun Map<LinkVisitOutcome, List<LinkVisit>>.getCount(outcome: LinkVisitOutcome) = this[outcome]?.size ?: 0

val KNOWN_FILE_SUFFIXES = setOf(
    "pdf",
    "mp3",
    "zip"
)

fun Url.isFile(): Boolean {
    return path.split(".").lastOrNull()?.let { it in KNOWN_FILE_SUFFIXES } ?: false
}

fun variabilityScore(path: String): Double {
    var score = 1.0
    for (char in path) {
        when (char) {
            '?', '#' -> break
            in 'a'..'z' -> score *= 26
            in 'A'..'Z' -> score *= 52
            in '0'..'9' -> score *= 62
            '/', '-' -> continue
        }
    }
    return score
}

val VARIABILITY_MIN_SCORE = 26.0.pow(20)