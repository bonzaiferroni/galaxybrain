package ponder.galaxy.server.db.services

import kabinet.model.SpeechGenRequest
import kabinet.model.SpeechVoice
import kabinet.utils.generateUuidString
import kabinet.utils.toAgoDescription
import klutch.gemini.GeminiService
import kotlinx.datetime.Clock
import ponder.galaxy.model.data.CommentId
import ponder.galaxy.model.data.Idea
import ponder.galaxy.model.data.IdeaId
import ponder.galaxy.model.data.StarId

class IdeaService(
    val dao: IdeaTableDao = IdeaTableDao(),
    private val starDao: StarTableDao = StarTableDao(),
    private val galaxyDao: GalaxyTableDao = GalaxyTableDao(),
    private val commentDao: CommentTableDao = CommentTableDao(),
    private val geminiService: GeminiService = GeminiService(),
    private val snippetService: SnippetTableService = SnippetTableService(),
    private val starSnippetDao: StarSnippetTableDao = StarSnippetTableDao(),
) {
    suspend fun createFromHeadline(starId: StarId): Idea {
        val star = starDao.readByIdOrNull(starId) ?: error("star not found: $starId")
        val galaxy = galaxyDao.readById(star.galaxyId)
        val speechText = "From ${galaxy.name}, posted ${(Clock.System.now() - star.createdAt).toAgoDescription()}.\n\n${star.title}"
        val voice = SpeechVoice.entries[galaxy.intrinsicIndex % SpeechVoice.entries.size]
        val audioUrl = geminiService.generateSpeech(SpeechGenRequest(
            text = speechText,
            filename = star.title,
            theme = "Say the following like you are a news reporter:",
            voice = voice
        ))
        println("IdeaService: $audioUrl")
        val idea = Idea(
            ideaId = IdeaId(generateUuidString()),
            starId = starId,
            commentId = null,
            description = IDEA_HEADLINE_DESCRIPTION,
            audioUrl = audioUrl,
            text = star.title,
            imageUrl = null,
            thumbUrl = null,
            createdAt = Clock.System.now()
        )
        dao.insert(idea)
        return idea
    }

    suspend fun createFromContent(starId: StarId): Idea? {
        val star = starDao.readByIdOrNull(starId) ?: error("star not found: $starId")
        val textContent = snippetService.dao.readStarSnippets(starId).joinToString("\n") { it.text }
        val galaxy = galaxyDao.readById(star.galaxyId)
        val voice = SpeechVoice.entries[galaxy.intrinsicIndex % SpeechVoice.entries.size]
        val audioUrl = geminiService.generateSpeech(SpeechGenRequest(
            text = textContent,
            filename = "${star.identifier}_content",
            theme = "Say the following in a conversational voice, in a style that matches the content, " +
                    "and don't be overly enthusiastic:",
            voice = voice
        ))
//        val imageUrls = star.imageUrl?.let { ImageUrls(it, star.thumbUrl ?: it) } ?: geminiService.generateImage(ImageGenRequest(
//            text = "Create an image based on the following content. " +
//                    "The image can capture the general essence of the content or focusing on one particular aspect.\n\n${textContent}",
//            theme = "Create the following image in the style of a cinematic lego scene. Do not include any text.",
//            filename = star.title
//        ))
        val idea = Idea(
            ideaId = IdeaId(generateUuidString()),
            starId = starId,
            commentId = null,
            description = IDEA_CONTENT_DESCRIPTION,
            audioUrl = audioUrl,
            text = star.title,
            imageUrl = null, // imageUrls.url,
            thumbUrl = null, // imageUrls.thumbUrl,
            createdAt = Clock.System.now()
        )
        dao.insert(idea)
        return idea
    }

    suspend fun createFromComment(commentId: CommentId): Idea? {
        val comment = commentDao.readByIdOrNull(commentId) ?: return null
        val star = starDao.readByIdOrNull(comment.starId) ?: return null
        val snippets = starSnippetDao.readByCommentId(commentId)
        val textContent = snippets.joinToString("\n")
        val voice = comment.getVoice()
        val audioUrl = geminiService.generateSpeech(SpeechGenRequest(
            text = textContent,
            filename = "${comment.identifier}_comment",
            theme = "Say the following in a conversational voice, in a style that matches the content, " +
                    "and don't be overly enthusiastic:",
            voice = voice
        ))
//        val imageUrls = star.imageUrl?.let { ImageUrls(it, star.thumbUrl ?: it) } ?: geminiService.generateImage(ImageGenRequest(
//            text = "Create an image based on the following content. " +
//                    "The image can capture the general essence of the content or focusing on one particular aspect.\n\n${textContent}",
//            theme = "Create the following image in the style of a cinematic lego scene. Do not include any text.",
//            filename = "${comment.identifier}_comment",
//        ))
        val idea = Idea(
            ideaId = IdeaId(generateUuidString()),
            starId = star.starId,
            commentId = comment.commentId,
            description = IDEA_COMMENT_DESCRIPTION,
            audioUrl = audioUrl,
            text = textContent,
            imageUrl = null, // imageUrls.url,
            thumbUrl = null, // imageUrls.thumbUrl,
            createdAt = Clock.System.now()
        )
        dao.insert(idea)
        return idea
    }
}

const val IDEA_HEADLINE_DESCRIPTION = "headline"
const val IDEA_CONTENT_DESCRIPTION = "content"
const val IDEA_COMMENT_DESCRIPTION = "comment"