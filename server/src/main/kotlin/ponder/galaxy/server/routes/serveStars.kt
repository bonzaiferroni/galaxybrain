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
import kotlin.uuid.ExperimentalUuidApi

fun Routing.serveStars(
    service: StarTableService = StarTableService(),
) {
    post(Api.Stars.Multi) { starIds, endpoint ->
        service.dao.readByIds(starIds)
    }

    get(Api.Stars, { StarId(it) }) { starId, endpoint ->
        service.dao.readByIdOrNull(starId)
    }

    post(Api.Stars.ByUrl) { url, endpoint ->
        var star = service.dao.readByUrl(url)
        if (star != null) return@post star
        val create = endpoint.create.readParamOrNull(call)
        println(create)
        if (create == true) {
            star = service.discoverStarFromUrl(url)
        }
        star
    }

    get(Api.Stars.Latest, { GalaxyId(it)}) { galaxyId, endpoint ->
        service.dao.readLatestByGalaxyId(galaxyId)
    }

    post(Api.Stars.NewContent) { newContent, endpoint ->
        service.createStarFromContent(newContent)
    }
}