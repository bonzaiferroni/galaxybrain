@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.routes

import io.ktor.server.routing.Routing
import klutch.server.get
import klutch.server.post
import ponder.galaxy.model.Api
import ponder.galaxy.model.data.NewUniverse
import ponder.galaxy.model.data.QuestionId
import ponder.galaxy.model.data.UniverseId
import ponder.galaxy.server.db.services.UniverseService
import ponder.galaxy.server.plugins.TableAccess
import kotlin.uuid.ExperimentalUuidApi

fun Routing.serveUniverses(
    tao: TableAccess = TableAccess(),
    service: UniverseService = UniverseService(),
) {
    get(Api.Universes, { UniverseId(it) }) { universeId, endpoint ->
        tao.universe.readByIdOrNull(universeId)
    }

    get(Api.Universes.ByQuestion, { QuestionId(it) }) { questionId, endpoint ->
        tao.universe.readByQuestion(questionId)
    }

    post(Api.Universes.Create) { newUniverse: NewUniverse, endpoint ->
        val universe = service.createUniverse(newUniverse)
        universe?.let { service.scanForUniverse(universe) }
        universe != null
    }
}
