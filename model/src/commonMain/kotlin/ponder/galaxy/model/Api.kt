package ponder.galaxy.model

import kabinet.api.*
import ponder.galaxy.model.data.Example
import ponder.galaxy.model.data.Galaxy
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.NewExample
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLog
import ponder.galaxy.model.data.StarLogId

object Api: ParentEndpoint(null, apiPrefix) {
    object Examples : GetByIdEndpoint<Example>(this, "/example") {
        object User : GetEndpoint<List<Example>>(this, "/user")
        object Create: PostEndpoint<NewExample, Long>(this)
        object Delete: DeleteEndpoint<Long>(this)
        object Update: UpdateEndpoint<Example>(this)
    }

    object Stars: GetByTableIdEndpoint<StarId, Star>(this, "/star") {
        object Multi : PostEndpoint<List<StarId>, List<Star>>(this, "multi")
    }

    object StarLogs: GetByTableIdEndpoint<StarId, List<StarLog>>(this, "/star_log") {
        object Multi : PostEndpoint<List<StarId>, Map<StarId, List<StarLog>>>(this, "/multi")
    }

    object Galaxies: GetByTableIdEndpoint<GalaxyId, Galaxy>(this, "/galaxy") {
        object All : GetEndpoint<List<Galaxy>>(this, "/all")
    }
}

val apiPrefix = "/api/v1"
