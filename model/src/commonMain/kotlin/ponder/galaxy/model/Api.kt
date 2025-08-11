package ponder.galaxy.model

import kabinet.api.*
import kabinet.clients.GeminiMessage
import kabinet.gemini.GeminiApi
import kabinet.model.ImageUrls
import kabinet.model.SpeechRequest
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

    object Gemini : ParentEndpoint(this, "/gemini"), GeminiApi {
        object Chat : PostEndpoint<List<GeminiMessage>, String>(this, "/chat")
        object Image : PostEndpoint<String, ImageUrls>(this, "/image")
        object GenerateSpeech: PostEndpoint<SpeechRequest, String>(this, "/generate-speech")

        override val chat = Chat
        override val image = Image
        override val speech = GenerateSpeech
    }
}

val apiPrefix = "/api/v1"
