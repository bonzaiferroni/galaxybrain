package ponder.galaxy.server.db.services

import kabinet.web.Url
import kabinet.web.fromHref
import klutch.db.DbService
import klutch.web.HtmlClient
import kabinet.web.toUrlOrNull
import kotlinx.datetime.Clock
import ponder.galaxy.model.data.NewStarContent
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId

class StarTableService(
    val dao: StarTableDao = StarTableDao(),
    private val galaxyService: GalaxyService = GalaxyService(),
    private val hostService: HostTableService = HostTableService(),
    private val snippetService: SnippetTableService = SnippetTableService(),
    private val htmlClient: HtmlClient = HtmlClient()
): DbService() {

    suspend fun discoverStarFromUrl(href: String): Star? = dbQuery {
        val url = href.toUrlOrNull() ?: return@dbQuery null
        val existingStar = dao.readByUrl(url)
        if (existingStar != null) {
            return@dbQuery existingStar
        }

        val host = hostService.dao.readByUrl(url) ?: hostService.createByUrl(url)

        val document = htmlClient.readUrl(href)

        val name = document?.publisherName ?: url.core
        val galaxy = galaxyService.readByNameAndHostId(host.hostId, name)
            ?: galaxyService.createByNameAndHostId(host.hostId, "https://${url.hostAddress}", name)

        val star = Star(
            starId = StarId.random(),
            galaxyId = galaxy.galaxyId,
            url = url.href,
            identifier = null,
            title = document?.title,
            link = href,
            thumbUrl = null,
            imageUrl = null,
            visibility = null,
            commentCount = null,
            voteCount = null,
            wordCount = document?.wordCount,
            updatedAt = Clock.System.now(),
            createdAt = Clock.System.now(),
            accessedAt = Clock.System.now(),
        )
        dao.insert(star)

        if (document != null && document.contents.isNotEmpty()) {
            snippetService.createOrUpdateStarSnippets(star.starId, document)
        }
        
        star
    }

    suspend fun createStarFromContent(newContent: NewStarContent) = dbQuery {
        var star = dao.readByIdOrNull(newContent.starId) ?: error("missing star: ${newContent.starId}")
        val document = if (newContent.isHtml) {
            htmlClient.readHtml(star.url, newContent.content)
        } else error("non html not yet supported")

        star = star.copy(
            title = document.title,
            wordCount = document.wordCount,
        )

        dao.update(star)

        if (document.contents.isNotEmpty()) {
            snippetService.createOrUpdateStarSnippets(star.starId, document)
        }

        true
    }
}