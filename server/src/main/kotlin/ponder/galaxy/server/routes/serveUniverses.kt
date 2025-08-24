@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.routes

import io.ktor.server.routing.Routing
import klutch.server.get
import klutch.server.post
import ponder.galaxy.model.Api
import ponder.galaxy.model.data.NewUniverse
import ponder.galaxy.model.data.QuestionId
import ponder.galaxy.model.data.UniverseId
import ponder.galaxy.server.db.services.UniverseTableService
import kotlin.uuid.ExperimentalUuidApi

fun Routing.serveUniverses(
    service: UniverseTableService = UniverseTableService(),
) {
    get(Api.Universes, { UniverseId(it) }) { universeId, endpoint ->
        service.dao.readByIdOrNull(universeId)
    }

    get(Api.Universes.ByQuestion, { QuestionId(it) }) { questionId, endpoint ->
        service.dao.readByQuestion(questionId)
    }

    post(Api.Universes.Create) { newUniverse: NewUniverse, endpoint ->
        service.createUniverse(newUniverse)
    }
}
