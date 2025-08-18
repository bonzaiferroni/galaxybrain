@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.services

import klutch.db.DbService
import klutch.utils.toUUID
import klutch.web.WebDocument
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.upsertReturning
import ponder.galaxy.model.data.Snippet
import ponder.galaxy.model.data.SnippetId
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLink
import ponder.galaxy.model.data.StarLinkId
import ponder.galaxy.model.data.StarSnippet
import ponder.galaxy.model.data.StarSnippetId
import ponder.galaxy.server.db.tables.SnippetTable
import ponder.galaxy.server.db.tables.toSnippet
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SnippetTableService(
    val dao: SnippetTableDao = SnippetTableDao(),
    private val starSnippetDao: StarSnippetTableDao = StarSnippetTableDao(),
    private val starLinkDao: StarLinkTableDao = StarLinkTableDao(),
    private val starDao: StarTableDao = StarTableDao(),
    private val snippetDao: SnippetTableDao = SnippetTableDao()
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
}
