@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.routes

import io.ktor.server.routing.Routing
import kabinet.utils.toUuid
import klutch.server.get
import ponder.galaxy.model.Api
import ponder.galaxy.model.data.StarId
import ponder.galaxy.server.db.services.StarLinkTableDao
import kotlin.uuid.ExperimentalUuidApi

fun Routing.serveStarLinks(
    dao: StarLinkTableDao = StarLinkTableDao(),
) {
    get(Api.StarLinks.Outgoing, { StarId(it)}) { starId, _ ->
        dao.readOutgoingByStarId(starId)
    }
}