package ponder.galaxy.server

import io.ktor.server.application.*
import kabinet.model.SpeechRequest
import klutch.db.generateMigrationScript
import klutch.environment.readEnvFromPath
import klutch.gemini.GeminiClient
import klutch.gemini.GeminiService
import klutch.gemini.generateSpeech
import klutch.gemini.message
import klutch.server.configureSecurity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ponder.galaxy.model.reddit.ListingType
import ponder.galaxy.model.reddit.REDDIT_APP_ID_KEY
import ponder.galaxy.model.reddit.REDDIT_APP_SECRET_KEY
import ponder.galaxy.model.reddit.REDDIT_PASSWORD_KEY
import ponder.galaxy.model.reddit.REDDIT_USERNAME_KEY
import ponder.galaxy.model.reddit.RedditAuth
import ponder.galaxy.model.reddit.RedditClient
import ponder.galaxy.model.reddit.flatten
import ponder.galaxy.server.io.RedditMonitor
import ponder.galaxy.server.plugins.configureApiRoutes
import ponder.galaxy.server.plugins.configureCors
import ponder.galaxy.server.plugins.configureDatabases
import ponder.galaxy.server.plugins.configureLogging
import ponder.galaxy.server.plugins.configureSerialization
import ponder.galaxy.server.plugins.configureWebSockets
import ponder.galaxy.server.plugins.dbTables
import java.io.File

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

    configureCors()
    configureSerialization()
    configureDatabases()
    configureSecurity()
    configureApiRoutes()
    configureWebSockets(redditMonitor, redditClient)
    configureLogging()

    redditMonitor.start()

    CoroutineScope(Dispatchers.IO).launch {
        // val comments = redditClient.getComments("Futurology", "1mgs91o").flatten()
        // println(comments.size)
//        val client = GeminiClient(
//            token = env.read("GEMINI_KEY_RATE_LIMIT_A"),
//            backupToken = env.read("GEMINI_KEY_RATE_LIMIT_B"),
//            logMessage = log::message,
//        )
//        val responseText = client.generateSpeech("testing 123", null, null) ?: error("arr matey")
//        File("gemini_speech_response.json").writeText(responseText)
    }
}

private val env = readEnvFromPath()