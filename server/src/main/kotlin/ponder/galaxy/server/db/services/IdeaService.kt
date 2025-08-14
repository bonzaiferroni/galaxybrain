package ponder.galaxy.server.db.services

import kabinet.model.ImageGenRequest
import kabinet.model.ImageUrls
import kabinet.model.SpeechGenRequest
import kabinet.model.SpeechVoice
import kabinet.utils.generateUuidString
import kabinet.utils.toAgoDescription
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
    suspend fun createFromHeadline(starId: StarId): Idea {
        val star = starDao.readByIdOrNull(starId) ?: error("star not found: $starId")
        val galaxy = galaxyDao.readById(star.galaxyId)
        val speechText = "From ${galaxy.name}, posted ${(Clock.System.now() - star.createdAt).toAgoDescription()}.\n\n${star.title}"
        val voice = SpeechVoice.entries[galaxy.intrinsicIndex % SpeechVoice.entries.size]
        val audioUrl = geminiClient.generateSpeech(SpeechGenRequest(
            text = speechText,
            filename = star.title,
            theme = "Say the following like you are a news reporter:",
            voice = voice
        ))
        println("IdeaService: $audioUrl")
        val idea = Idea(
            ideaId = IdeaId(generateUuidString()),
            starId = starId,
            description = IDEA_HEADLINE_DESCRIPTION,
            audioUrl = audioUrl,
            text = star.title,
            imageUrl = null,
            thumbUrl = null,
            createdAt = Clock.System.now()
        )
        ideaDao.insert(idea)
        return idea
    }

    suspend fun createFromContent(starId: StarId): Idea? {
        val star = starDao.readByIdOrNull(starId) ?: error("star not found: $starId")
        val textContent = star.textContent ?: return null
        val galaxy = galaxyDao.readById(star.galaxyId)
        val voice = SpeechVoice.entries[galaxy.intrinsicIndex % SpeechVoice.entries.size]
        val audioUrl = geminiClient.generateSpeech(SpeechGenRequest(
            text = textContent,
            filename = "${star.identifier}_content",
            theme = "Say the following in a conversational voice, don't be overly enthusiastic:",
            voice = voice
        ))
        val imageUrls = star.imageUrl?.let { ImageUrls(it, star.thumbUrl ?: it) } ?: geminiClient.generateImage(ImageGenRequest(
            text = "Create an image based on the following content. " +
                    "The image can capture the general essence of the content or focusing on one particular aspect.\n\n${textContent}",
            theme = "Create the following image in the style of a cinematic lego scene. Do not include any text.",
            filename = star.title
        ))
        val idea = Idea(
            ideaId = IdeaId(generateUuidString()),
            starId = starId,
            description = IDEA_CONTENT_DESCRIPTION,
            audioUrl = audioUrl,
            text = star.title,
            imageUrl = imageUrls.url,
            thumbUrl = imageUrls.thumbUrl,
            createdAt = Clock.System.now()
        )
        ideaDao.insert(idea)
        return idea
    }

//    suspend fun createFromComments(starId: StarId): Idea {
//
//    }
}

const val IDEA_HEADLINE_DESCRIPTION = "headline"
const val IDEA_CONTENT_DESCRIPTION = "content"