package ponder.galaxy.server.db.services

import kabinet.web.Url
import kabinet.web.fromHref
import klutch.db.DbService
import klutch.web.HtmlClient
import kabinet.web.toUrlOrNull
import klutch.db.read
import klutch.utils.greaterEq
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.galaxy.model.data.NewStarContent
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import ponder.galaxy.server.db.tables.StarTable
import ponder.galaxy.server.db.tables.toStar
import ponder.galaxy.server.plugins.TableAccess

class StarTableService(
    private val tao: TableAccess = TableAccess(),
    private val galaxyService: GalaxyService = GalaxyService(),
    private val hostService: HostTableService = HostTableService(),
    private val snippetService: SnippetTableService = SnippetTableService(),
    private val htmlClient: HtmlClient = HtmlClient()
): DbService() {

    suspend fun discoverStarFromUrl(href: String, visitDocument: Boolean): Star? = dbQuery {
        val url = href.toUrlOrNull() ?: return@dbQuery null
        val existingStar = tao.star.readByUrl(url)
        if (existingStar != null) {
            return@dbQuery existingStar
        }

        val host = tao.host.readByUrl(url) ?: hostService.createByUrl(url)

        val document = if (visitDocument) htmlClient.readUrl(href) else null

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
            accessedAt = if (document != null) Clock.System.now() else null,
            publishedAt = document?.publishedAt,
            updatedAt = Clock.System.now(),
            createdAt = Clock.System.now(),
        )
        tao.star.insert(star)

        if (document != null && document.contents.isNotEmpty()) {
            snippetService.createOrUpdateStarSnippets(star.starId, document)
        }

        // todo: associate starlinks
        
        star
    }

    suspend fun createStarFromContent(newContent: NewStarContent) = dbQuery {
        var star = tao.star.readByIdOrNull(newContent.starId) ?: error("missing star: ${newContent.starId}")
        val document = if (newContent.isHtml) {
            htmlClient.readHtml(star.url, newContent.content)
        } else error("non html not yet supported")

        star = star.copy(
            title = document.title,
            wordCount = document.wordCount,
        )

        tao.star.update(star)

        if (document.contents.isNotEmpty()) {
            snippetService.createOrUpdateStarSnippets(star.starId, document)
        }

        true
    }
}