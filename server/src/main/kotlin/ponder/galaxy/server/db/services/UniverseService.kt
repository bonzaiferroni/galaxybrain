package ponder.galaxy.server.db.services

import kabinet.console.globalConsole
import kabinet.utils.sumOfFloat
import klutch.db.DbService
import klutch.db.cosineDistance
import klutch.db.readByIdOrNull
import klutch.db.readSingleOrNull
import klutch.gemini.GeminiService
import klutch.utils.betweenNullable
import klutch.utils.eq
import klutch.utils.toUUID
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.innerJoin
import ponder.galaxy.model.data.CommentId
import ponder.galaxy.model.data.NewUniverse
import ponder.galaxy.model.data.Signal
import ponder.galaxy.model.data.SignalId
import ponder.galaxy.model.data.SignalScan
import ponder.galaxy.model.data.SignalScanId
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarSnippet
import ponder.galaxy.model.data.Universe
import ponder.galaxy.model.data.UniverseId
import ponder.galaxy.server.db.tables.CommentTable
import ponder.galaxy.server.db.tables.SnippetEmbeddingTable
import ponder.galaxy.server.db.tables.StarSnippetTable
import ponder.galaxy.server.db.tables.StarTable
import ponder.galaxy.server.db.tables.UniverseTable
import ponder.galaxy.server.db.tables.existedAt
import ponder.galaxy.server.db.tables.toStarSnippet
import ponder.galaxy.server.db.tables.toUniverse
import ponder.galaxy.server.plugins.TableAccess
import kotlin.collections.plus
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private val console = globalConsole.getHandle(UniverseService::class)

class UniverseService(
    private val tao: TableAccess = TableAccess(),
    private val geminiService: GeminiService = GeminiService(),
) : DbService() {
    suspend fun createUniverse(newUniverse: NewUniverse): Universe? = dbQuery {
        val definition = newUniverse.definition.trim()
        println("ey: $definition")
        if (definition.isEmpty()) return@dbQuery null
        // val question = questionDao.readByIdOrNull(newUniverse.questionId) ?: error("Question not found")
        val universeCount = tao.universe.countByQuestionId(newUniverse.questionId)
        val embedding = geminiService.generateEmbedding(definition) ?: error("Universe vector not found")
        val label = universeCount.toInt().toGreekLetter() ?: "New Universe"
        val now = Clock.System.now()
        val universe = Universe(
            universeId = UniverseId.random(),
            questionId = newUniverse.questionId,
            label = label,
            definition = definition,
            imgUrl = null,
            thumbUrl = null,
            interval = 60 * 24,
            coherence = null,
            signal = null,
            embedding = embedding,
            createdAt = now
        )
        tao.universe.insert(universe)
        console.log("Created universe: $label")

        universe
    }

    suspend fun scanForUniverse(universe: Universe) {
        val interval = universe.interval.minutes
        val now = Clock.System.now()
        scanForUniverse(universe.universeId, now - interval, interval)
    }

    suspend fun scanForUniverse(universeId: UniverseId, start: Instant, interval: Duration, limit: Int = 100) = dbQuery {
        val universe = tao.universe.readByIdOrNull(universeId) ?: error("universe not found")
        val distance = SnippetEmbeddingTable.vector.cosineDistance(universe.embedding).alias("cosine_distance")
        val end = start + interval
        val distances = StarTable.innerJoin(StarSnippetTable, { StarTable.id }, { StarSnippetTable.starId })
            .innerJoin(SnippetEmbeddingTable, { StarSnippetTable.snippetId }, { SnippetEmbeddingTable.id })
            .select(StarSnippetTable.columns + distance)
            .where { StarTable.existedAt().betweenNullable(start, end) }
            .orderBy(distance, SortOrder.ASC)
            .limit(limit)
            .map { Pair(it[distance], it.toStarSnippet()) }
        val signalScanId = SignalScanId.random()
        val now = Clock.System.now()
        val signals = distances.filter { it.first < .4f }.map { (distance, starSnippet) ->
            val visibility = readVisibility(starSnippet.starId, starSnippet.commentId) ?: 0f
            Signal(
                signalId = SignalId.random(),
                starSnippetId = starSnippet.starSnippetId,
                signalScanId = signalScanId,
                distance = distance,
                visibility = visibility,
                createdAt = now
            )
        }

        val signalScan = SignalScan(
            signalScanId = signalScanId,
            universeId = universeId,
            sum = signals.sumOfFloat { it.visibility },
            count = signals.size,
            createdAt = now,
        )

        tao.signalScan.insert(signalScan)
        tao.signal.insert(signals)
        console.log("Universe scan found ${signals.size} signals")
    }

    suspend fun readVisibility(starId: StarId, commentId: CommentId?) = dbQuery {
        if (commentId != null) {
            CommentTable.readSingleOrNull(listOf(CommentTable.visibility)) { it.id.eq(commentId) }
                ?.let { it[CommentTable.visibility] }
        } else {
            StarTable.readSingleOrNull(listOf(StarTable.visibility)) { it.id.eq(starId) }?.let { it[StarTable.visibility] }
        }
    }
}

private fun Int.toGreekLetter(): String? {
    return greekLetters.getOrNull(this)
}

private val greekLetters = listOf(
    "Alpha", "Beta", "Gamma", "Delta", "Epsilon",
    "Zeta", "Eta", "Theta", "Iota", "Kappa",
    "Lambda", "Mu", "Nu", "Xi", "Omicron",
    "Pi", "Rho", "Sigma", "Tau", "Upsilon",
    "Phi", "Chi", "Psi", "Omega"
)

@Serializable
data class StarSnippetDistance(
    val distance: Float,
    val starSnippet: StarSnippet,
)
