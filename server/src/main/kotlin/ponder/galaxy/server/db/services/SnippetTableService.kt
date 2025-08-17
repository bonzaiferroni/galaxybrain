@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.services

import klutch.db.DbService
import klutch.web.WebDocument
import kotlinx.datetime.Clock
import ponder.galaxy.model.data.Snippet
import ponder.galaxy.model.data.SnippetId
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLink
import ponder.galaxy.model.data.StarLinkId
import ponder.galaxy.model.data.StarSnippet
import ponder.galaxy.model.data.StarSnippetId
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SnippetTableService(
    val dao: SnippetTableDao = SnippetTableDao(),
    private val starSnippetDao: StarSnippetTableDao = StarSnippetTableDao(),
    private val starLinkDao: StarLinkTableDao = StarLinkTableDao(),
    private val starDao: StarTableDao = StarTableDao(),
    private val snippetDao: SnippetTableDao = SnippetTableDao()
) : DbService() {

    suspend fun createOrUpdateFromStarDocument(starId: StarId, document: WebDocument) = dbQuery {
        val starSnippets = snippetDao.readByStarId(starId)
        if (starSnippets.isNotEmpty()) return@dbQuery // todo: update snippets

        val now = Clock.System.now()
        document.contents.forEachIndexed { index, content ->
            val snippet = readOrCreateByText(content.text)

            val starSnippetId = StarSnippetId(Uuid.random())
            starSnippetDao.insert(StarSnippet(
                starSnippetId = starSnippetId,
                snippetId = snippet.snippetId,
                starId = starId,
                commentId = null,
                index = index,
                createdAt = now,
            ))

            content.links.forEach { link ->
                val starLinkId = StarLinkId(Uuid.random())
                val toStar = starDao.readByUrl(link.url)
                starLinkDao.insert(StarLink(
                    starLinkId = starLinkId,
                    fromStarId = starId,
                    toStarId = toStar?.starId,
                    snippetId = snippet.snippetId,
                    url = link.url,
                    text = link.text,
                    startIndex = link.startIndex,
                    createdAt = now
                ))
            }
        }
    }

    suspend fun readOrCreateByText(text: String) = dbQuery {
        var snippet = dao.readByText(text)
        if (snippet != null) return@dbQuery snippet

        val snippetId = SnippetId(Uuid.random())
        snippet = Snippet(
            snippetId = snippetId,
            text = text
        )
        dao.insert(snippet)
        snippet
    }
}
