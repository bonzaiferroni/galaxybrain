package ponder.galaxy.server.io

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
                        println("EmbeddingWriter: missing embedding")
                        continue
                    }
                    snippetService.dao.insert(SnippetEmbedding(
                        snippetId = snippet.snippetId,
                        vector = vector
                    ))
                    delay(1.seconds)
                }
                println("wrote: ${snippets.size} embeddings")
                delay(1.minutes)
            }
        }
    }
}

const val MIN_EMBEDDING_CHARS = 250