@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.services

import kabinet.model.SpeechGenRequest
import kabinet.model.SpeechVoice
import klutch.db.DbService
import klutch.db.cosineDistance
import klutch.gemini.GeminiService
import klutch.gemini.KokoroClient
import klutch.utils.toUUID
import klutch.web.WebDocument
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.charLength
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.upsertReturning
import ponder.galaxy.model.data.Snippet
import ponder.galaxy.model.data.SnippetAudio
import ponder.galaxy.model.data.SnippetId
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLink
import ponder.galaxy.model.data.StarLinkId
import ponder.galaxy.model.data.StarSnippet
import ponder.galaxy.model.data.StarSnippetId
import ponder.galaxy.model.data.SnippetDistance
import ponder.galaxy.server.db.tables.SnippetEmbeddingTable
import ponder.galaxy.server.db.tables.SnippetTable
import ponder.galaxy.server.db.tables.toSnippet
import kotlin.uuid.ExperimentalUuidApi

class SnippetTableService(
    val dao: SnippetTableDao = SnippetTableDao(),
    val audioDao: SnippetAudioTableDao = SnippetAudioTableDao(),
    private val starSnippetDao: StarSnippetTableDao = StarSnippetTableDao(),
    private val starLinkDao: StarLinkTableDao = StarLinkTableDao(),
    private val starDao: StarTableDao = StarTableDao(),
    private val snippetDao: SnippetTableDao = SnippetTableDao(),
    private val kokoroClient: KokoroClient = KokoroClient(),
    private val geminiService: GeminiService = GeminiService(),
) : DbService() {

    suspend fun createOrUpdateStarSnippets(starId: StarId, document: WebDocument) = dbQuery {
        val starSnippets = snippetDao.readStarSnippets(starId)
        if (starSnippets.isNotEmpty()) return@dbQuery // todo: update snippets

        val now = Clock.System.now()
        document.contents.forEachIndexed { index, content ->
            val snippet = readOrCreateByText(content.text)

            val starSnippetId = StarSnippetId.random()
            starSnippetDao.insert(
                StarSnippet(
                    starSnippetId = starSnippetId,
                    snippetId = snippet.snippetId,
                    starId = starId,
                    commentId = null,
                    order = index,
                    createdAt = now,
                )
            )

            content.links.forEach { link ->
                val starLinkId = StarLinkId.random()
                val toStar = starDao.readByUrl(link.url)
                starLinkDao.insert(
                    StarLink(
                        starLinkId = starLinkId,
                        fromStarId = starId,
                        toStarId = toStar?.starId,
                        snippetId = snippet.snippetId,
                        commentId = null,
                        url = link.url,
                        text = link.text,
                        startIndex = link.startIndex,
                        createdAt = now
                    )
                )
            }
        }
    }

    suspend fun readOrCreateByText(text: String) = dbQuery {
        var snippet = dao.readByText(text)
        if (snippet != null) return@dbQuery snippet

        val snippetId = SnippetId.random()
        snippet = Snippet(
            snippetId = snippetId,
            text = text
        )
        dao.insert(snippet)
        snippet
    }

    suspend fun readOrCreateByTextAlt(text: String) = dbQuery {
        SnippetTable.upsertReturning(
            keys = arrayOf(SnippetTable.text),
            returning = listOf(SnippetTable.id, SnippetTable.text),
            onUpdate = {
                it[SnippetTable.text] = insertValue(SnippetTable.text)
            }
        ) { st ->
            st[SnippetTable.id] = SnippetId.random().toUUID()
            st[SnippetTable.text] = text
        }
            .single().toSnippet()
    }

    suspend fun readOrCreateAudio(snippetId: SnippetId) = dbQuery {
        var audio = audioDao.readByIdOrNull(snippetId)
        if (audio != null) {
            return@dbQuery audio
        }
        val snippet = dao.readById(snippetId) ?: return@dbQuery null
        println("audio: ${snippet.text.take(40)}")
        val path = kokoroClient.generateSpeech(
            SpeechGenRequest(
                text = snippet.text,
                theme = "Say the following in a conversational voice, in a style that matches the content, " +
                        "and don't be overly enthusiastic:",
                voice = SpeechVoice.Isabella,
                filename = snippet.text
            )
        )

        audio = SnippetAudio(
            snippetId = snippetId,
            path = path,
        )
        audioDao.upsert(audio)
        audio
    }

    suspend fun readMissingEmbeddings(minCharacters: Int = 200) = dbQuery {
        SnippetTable.leftJoin(SnippetEmbeddingTable, { id }, { id })
            .select(SnippetTable.columns)
            .where { SnippetEmbeddingTable.vector.isNull() and SnippetTable.text.charLength().greaterEq(minCharacters) }
            .map { it.toSnippet() }
    }

    suspend fun testUniverse(universe: String, limit: Int = 10) = dbQuery {
        val universeVector = geminiService.generateEmbedding(universe) ?: error("Universe vector not found")
        val distance = SnippetEmbeddingTable.vector.cosineDistance(universeVector).alias("cosine_distance")
        SnippetEmbeddingTable.innerJoin(SnippetTable, { id }, { id })
            .select(SnippetTable.columns + distance)
            .orderBy(distance, SortOrder.ASC)
            .limit(limit)
            .map { SnippetDistance(it[distance], it.toSnippet()) }
    }
}
