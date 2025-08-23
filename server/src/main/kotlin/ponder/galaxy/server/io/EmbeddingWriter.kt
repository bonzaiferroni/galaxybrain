package ponder.galaxy.server.io

import kabinet.console.globalConsole
import klutch.gemini.GeminiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ponder.galaxy.model.data.SnippetEmbedding
import ponder.galaxy.server.db.services.SnippetTableService
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val console = globalConsole.getHandle(EmbeddingWriter::class)

class EmbeddingWriter(
    private val geminiService: GeminiService = GeminiService(),
    private val snippetService: SnippetTableService = SnippetTableService()
) {
    private var job: Job? = null
    fun start() {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val snippets = snippetService.readMissingEmbeddings(MIN_EMBEDDING_CHARS)
                for (snippet in snippets) {
                    val vector = geminiService.generateEmbedding(snippet.text)
                    if (vector == null) {
                        console.logError("missing embedding")
                        continue
                    }
                    snippetService.dao.insert(SnippetEmbedding(
                        snippetId = snippet.snippetId,
                        vector = vector
                    ))
                    delay(1.seconds)
                }
                if (snippets.isNotEmpty()) {
                    console.log("wrote: ${snippets.size} embeddings")
                }
                delay(1.minutes)
            }
        }
    }
}

const val MIN_EMBEDDING_CHARS = 100