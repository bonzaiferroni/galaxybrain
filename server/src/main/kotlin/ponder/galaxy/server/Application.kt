package ponder.galaxy.server

import io.ktor.server.application.*
import klutch.db.generateMigrationScript
import klutch.environment.readEnvFromPath
import klutch.gemini.GeminiService
import klutch.server.configureSecurity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ponder.galaxy.model.reddit.REDDIT_APP_ID_KEY
import ponder.galaxy.model.reddit.REDDIT_APP_SECRET_KEY
import ponder.galaxy.model.reddit.REDDIT_PASSWORD_KEY
import ponder.galaxy.model.reddit.REDDIT_USERNAME_KEY
import ponder.galaxy.model.reddit.RedditAuth
import ponder.galaxy.model.reddit.RedditClient
import ponder.galaxy.server.io.EmbeddingWriter
import ponder.galaxy.server.io.LinkScanner
import ponder.galaxy.server.io.RedditMonitor
import ponder.galaxy.server.plugins.configureApiRoutes
import ponder.galaxy.server.plugins.configureCors
import ponder.galaxy.server.plugins.configureDatabases
import ponder.galaxy.server.plugins.configureLogging
import ponder.galaxy.server.plugins.configureSerialization
import ponder.galaxy.server.plugins.configureWebSockets
import ponder.galaxy.server.plugins.dbTables

fun main(args: Array<String>) {
    if ("migrate" in args) generateMigrationScript(readEnvFromPath(), dbTables)
    else io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val redditClient = RedditClient(
        RedditAuth(
            username = env.read(REDDIT_USERNAME_KEY),
            password = env.read(REDDIT_PASSWORD_KEY),
            appId = env.read(REDDIT_APP_ID_KEY),
            appSecret = env.read(REDDIT_APP_SECRET_KEY)
        )
    )
    val redditMonitor = RedditMonitor(redditClient)
    val embeddingWriter = EmbeddingWriter()
    val linkScanner = LinkScanner()

    configureCors()
    configureSerialization()
    configureDatabases()
    configureSecurity()
    configureApiRoutes()
    configureWebSockets(redditMonitor, redditClient)
    configureLogging()

    redditMonitor.start()
    embeddingWriter.start()
    linkScanner.start()

    CoroutineScope(Dispatchers.IO).launch {
        // val geminiService = GeminiService()
        // val vector = geminiService.generateEmbedding("hello world")
        // println(vector?.joinToString()?.take(40))
//        val client = KokoroClient()
//        val bytes = client.getMessage("hello world")
//        File("msg.wav").writeBytes(bytes)
        // val htmlClient = HtmlClient()
//        val content = htmlClient.readUrl("https://colton.dev/blog/curing-your-ai-10x-engineer-imposter-syndrome/") ?: return@launch
//        File("content/colton.md").writeText(content.toMarkdown())
//        val content = htmlClient.readUrl("https://www.theverge.com/ai-artificial-intelligence/759965/sam-altman-openai-ai-bubble-interview") ?: return@launch
//        File("content/theverge.md").writeText(content.toMarkdown())
    }
}

private val env = readEnvFromPath()