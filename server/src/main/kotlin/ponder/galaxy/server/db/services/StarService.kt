package ponder.galaxy.server.db.services

import klutch.db.DbService
import klutch.web.HtmlClient
import kabinet.web.toUrlOrNull
import kotlinx.datetime.Clock
import kabinet.utils.generateUuidString
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId

class StarService(
    val dao: StarTableDao = StarTableDao(),
    private val galaxyService: GalaxyService = GalaxyService(),
    private val hostService: HostTableService = HostTableService(),
    private val snippetService: SnippetTableService = SnippetTableService(),
    private val htmlClient: HtmlClient = HtmlClient()
): DbService() {

    suspend fun discoverStarFromUrl(href: String): StarId? = dbQuery {
        val url = href.toUrlOrNull() ?: return@dbQuery null
        val existingStar = dao.readByUrl(url)
        if (existingStar != null) {
            return@dbQuery existingStar.starId
        }

        val host = hostService.dao.readByUrl(url) ?: hostService.createByUrl(url)

        val galaxy = galaxyService.readUnchartedByHostId(host.hostId) 
            ?: galaxyService.createUnchartedByHostId(host.hostId, "https://${url.hostAddress}")

        val document = htmlClient.readUrl(href)

        val star = Star(
            starId = StarId(generateUuidString()),
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

        if (document != null && document.contents.isNotEmpty()) {
            snippetService.createFromStarDocument(star.starId, document)
        }
        
        star.starId
    }
}