package ponder.galaxy.server.db.services

import kabinet.model.SpeechRequest
import kabinet.model.SpeechVoice
import kabinet.utils.generateUuidString
import kabinet.utils.toDayDescription
import kabinet.utils.toTimeDescription
import klutch.gemini.GeminiService
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
        val speechText = "From ${galaxy.name}, posted on ${star.createdAt.toDayDescription()} " +
                "at ${star.createdAt.toTimeDescription()}.\n\n${star.title}"
        val voice = SpeechVoice.entries[galaxy.intrinsicIndex % SpeechVoice.entries.size]
        val audioUrl = geminiClient.generateSpeech(SpeechRequest(
            text = speechText,
            filename = star.title,
            voice = voice
        ))
        val imageText = "Create an image that captures the essence of the following headline: ${star.title}"
        val imageUrl = geminiClient.generateImage(imageText)
        println("IdeaService: $audioUrl")
        val idea = Idea(
            ideaId = IdeaId(generateUuidString()),
            audioUrl = audioUrl,
            text = star.title,
            imageUrl = imageUrl.url,
            thumbUrl = imageUrl.thumbUrl
        )
        ideaDao.insert(idea, starId)
        return idea
    }
}