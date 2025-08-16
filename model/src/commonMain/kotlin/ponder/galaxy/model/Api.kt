package ponder.galaxy.model

import kabinet.api.*
import kabinet.clients.GeminiMessage
import kabinet.gemini.GeminiApi
import kabinet.model.ImageGenRequest
import kabinet.model.ImageUrls
import kabinet.model.SpeechGenRequest
import ponder.galaxy.model.data.CommentId
import ponder.galaxy.model.data.Example
import ponder.galaxy.model.data.ExampleId
import ponder.galaxy.model.data.Galaxy
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.Idea
import ponder.galaxy.model.data.NewExample
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLink
import ponder.galaxy.model.data.StarLog

object Api: ApiNode(ApiNode(null, "api"), "v1") {
    object Examples : GetByTableIdEndpoint<ExampleId, Example>(this, "example") {
        object User : GetEndpoint<List<Example>>(this, "user")
        object Create: PostEndpoint<NewExample, Long>(this)
        object Delete: DeleteEndpoint<Long>(this)
        object Update: UpdateEndpoint<Example>(this)
    }

    object Stars: GetByTableIdEndpoint<StarId, Star>(this, "star") {
        object Multi : PostEndpoint<List<StarId>, List<Star>>(this, "multi")
        object ByUrl : PostEndpoint<String, Star>(this, "url")
    }

    object StarLinks: ApiNode(this, "star_link") {
        object Outgoing : GetByTableIdEndpoint<StarId, List<StarLink>>(this, "links")
    }

    object StarLogs: GetByTableIdEndpoint<StarId, List<StarLog>>(this, "star_log") {
        object Multi : PostEndpoint<List<StarId>, Map<StarId, List<StarLog>>>(this, "multi")
    }

    object Galaxies: GetByTableIdEndpoint<GalaxyId, Galaxy>(this, "galaxy") {
        object All : GetEndpoint<List<Galaxy>>(this, "all")
    }

    object Gemini : ApiNode(this, "gemini"), GeminiApi {
        object Chat : PostEndpoint<List<GeminiMessage>, String>(this, "chat")
        object Image : PostEndpoint<ImageGenRequest, ImageUrls>(this, "image")
        object GenerateSpeech: PostEndpoint<SpeechGenRequest, String>(this, "generate-speech")

        override val chat = Chat
        override val image = Image
        override val speech = GenerateSpeech
    }

    object Ideas : GetEndpoint<List<Idea>>(this, "idea") {
        val since = addInstantParam("since")

        object Headline : GetByTableIdEndpoint<StarId, Idea?>(this, "headline") {
            val create = addBooleanParam("create")
        }
        object Content : GetByTableIdEndpoint<StarId, Idea?>(this, "content") {
            val create = addBooleanParam("create")
        }
        object Comment : GetByTableIdEndpoint<CommentId, Idea?>(this, "comment") {
            val create = addBooleanParam("create")
        }
    }
}
