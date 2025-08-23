package ponder.galaxy.app.io

import kabinet.api.write
import kotlinx.datetime.Instant
import ponder.galaxy.model.Api
import ponder.galaxy.model.data.CommentId
import ponder.galaxy.model.data.StarId
import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient

class IdeaApiClient(
    private val client: NeoApiClient = globalNeoApiClient
) {
    suspend fun readIdeas(since: Instant) = client.request(Api.Ideas) { write(it.since, since) }

    suspend fun readHeadlineIdea(starId: StarId, create: Boolean = false) =
        client.getById(Api.Ideas.Headline, starId) { write(it.create, create) }

    suspend fun readContentIdea(starId: StarId, create: Boolean = false) =
        client.getById(Api.Ideas.Content, starId) { write(it.create, create) }

    suspend fun readCommentIdea(commentId: CommentId, create: Boolean = false) =
        client.getById(Api.Ideas.Comments, commentId) { write(it.create, create) }
}