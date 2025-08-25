@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.routes

import io.ktor.server.routing.Routing
import kabinet.utils.toUuid
import klutch.server.get
import klutch.server.post
import klutch.server.readParamOrNull
import ponder.galaxy.model.Api
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.StarId
import ponder.galaxy.server.db.services.StarTableService
import ponder.galaxy.server.plugins.TableAccess
import kotlin.uuid.ExperimentalUuidApi

fun Routing.serveStars(
    tao: TableAccess = TableAccess(),
    service: StarTableService = StarTableService(),
) {
    post(Api.Stars.Multi) { starIds, endpoint ->
        tao.star.readByIds(starIds)
    }

    get(Api.Stars, { StarId(it) }) { starId, endpoint ->
        tao.star.readByIdOrNull(starId)
    }

    post(Api.Stars.ByUrl) { url, endpoint ->
        var star = tao.star.readByUrl(url)
        if (star != null) return@post star
        val create = endpoint.create.readParamOrNull(call)
        if (create == true) {
            star = service.discoverStarFromUrl(url, true)
        }
        star
    }

    get(Api.Stars.Latest, { GalaxyId(it)}) { galaxyId, endpoint ->
        tao.star.readLatestByGalaxyId(galaxyId)
    }

    post(Api.Stars.NewContent) { newContent, endpoint ->
        service.createStarFromContent(newContent)
    }
}