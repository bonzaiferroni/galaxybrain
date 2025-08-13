package ponder.galaxy.server.db.services

import kabinet.model.ImageGenRequest
import kabinet.model.ImageUrls
import kabinet.model.SpeechGenRequest
import kabinet.model.SpeechVoice
import kabinet.utils.generateUuidString
import kabinet.utils.toAgoDescription
import kabinet.utils.toDayDescription
import kabinet.utils.toTimeDescription
import klutch.gemini.GeminiService
import kotlinx.datetime.Clock
import ponder.galaxy.model.data.Idea
import ponder.galaxy.model.data.IdeaId
import ponder.galaxy.model.data.StarId

class IdeaService(
    val ideaDao: IdeaTableDao = IdeaTableDao(),
    val starDao: StarTableDao = StarTableDao(),
    val galaxyDao: GalaxyTableDao = GalaxyTableDao(),
    val geminiClient: GeminiService = GeminiService(),
) {
    suspend fun readOrCreateByStarId(starId: StarId) = ideaDao.readByStarId(starId).takeIf { it.isNotEmpty() }
        ?: listOf(createFromTitleByStarId(starId))

    suspend fun createFromTitleByStarId(starId: StarId): Idea {
        val star = starDao.readByIdOrNull(starId) ?: error("star not found: $starId")
        val galaxy = galaxyDao.readById(star.galaxyId)
        val speechText = "From ${galaxy.name}, posted ${(Clock.System.now() - star.createdAt).toAgoDescription()}.\n\n${star.title}"
        val voice = SpeechVoice.entries[galaxy.intrinsicIndex % SpeechVoice.entries.size]
        val audioUrl = geminiClient.generateSpeech(SpeechGenRequest(
            text = speechText,
            filename = star.title,
            voice = voice
        ))
        val imageUrls = star.imageUrl?.let { ImageUrls(it, star.thumbUrl ?: it) } ?: geminiClient.generateImage(ImageGenRequest(
            text = "Create an image that captures the essence of the following headline which was found in the subreddit ${galaxy.name}: ${star.title}",
            theme = "Create the image in the style of a cinematic lego scene. Do not include any text.",
            filename = star.title
        ))
        println("IdeaService: $audioUrl")
        val idea = Idea(
            ideaId = IdeaId(generateUuidString()),
            audioUrl = audioUrl,
            text = star.title,
            imageUrl = imageUrls.url,
            thumbUrl = imageUrls.thumbUrl,
            createdAt = Clock.System.now()
        )
        ideaDao.insert(idea, starId)
        return idea
    }
}