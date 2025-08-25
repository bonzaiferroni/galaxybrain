package ponder.galaxy.server.plugins

import io.ktor.server.application.*
import klutch.db.initDb
import klutch.db.tables.RefreshTokenTable
import klutch.db.tables.UserTable
import klutch.environment.readEnvFromPath
import ponder.galaxy.server.db.services.CommentTableDao
import ponder.galaxy.server.db.services.GalaxyTableDao
import ponder.galaxy.server.db.services.HostTableDao
import ponder.galaxy.server.db.services.IdeaTableDao
import ponder.galaxy.server.db.services.LinkVisitTableDao
import ponder.galaxy.server.db.services.QuestionTableDao
import ponder.galaxy.server.db.services.SignalScanTableDao
import ponder.galaxy.server.db.services.SignalTableDao
import ponder.galaxy.server.db.services.SnippetEmbeddingTableDao
import ponder.galaxy.server.db.services.SnippetTableDao
import ponder.galaxy.server.db.services.StarLinkTableDao
import ponder.galaxy.server.db.services.StarLogTableDao
import ponder.galaxy.server.db.services.StarSnippetTableDao
import ponder.galaxy.server.db.services.StarTableDao
import ponder.galaxy.server.db.services.UniverseTableDao
import ponder.galaxy.server.db.tables.CommentTable
import ponder.galaxy.server.db.tables.GalaxyTable
import ponder.galaxy.server.db.tables.IdeaTable
import ponder.galaxy.server.db.tables.StarLinkTable
import ponder.galaxy.server.db.tables.StarLogTable
import ponder.galaxy.server.db.tables.StarTable
import ponder.galaxy.server.db.tables.StarSnippetTable
import ponder.galaxy.server.db.tables.HostTable
import ponder.galaxy.server.db.tables.LinkVisitTable
import ponder.galaxy.server.db.tables.QuestionTable
import ponder.galaxy.server.db.tables.UniverseTable
import ponder.galaxy.server.db.tables.SignalTable
import ponder.galaxy.server.db.tables.SignalScanTable
import ponder.galaxy.server.db.tables.SnippetAudioTable
import ponder.galaxy.server.db.tables.SnippetEmbeddingTable
import ponder.galaxy.server.db.tables.SnippetTable

fun Application.configureDatabases() {
    initDb(env, dbTables)
}

val env = readEnvFromPath()

val dbTables = listOf(
    UserTable,
    RefreshTokenTable,
    StarTable,
    StarLogTable,
    GalaxyTable,
    IdeaTable,
    CommentTable,
    StarLinkTable,
    SnippetTable,
    SnippetAudioTable,
    SnippetEmbeddingTable,
    StarSnippetTable,
    HostTable,
    LinkVisitTable,
    QuestionTable,
    UniverseTable,
    SignalScanTable,
    SignalTable, 
)

class TableAccess {
    val star = StarTableDao()
    val starLog = StarLogTableDao()
    val galaxy = GalaxyTableDao()
    val idea = IdeaTableDao()
    val comment = CommentTableDao()
    val starLink = StarLinkTableDao()
    val snippet = SnippetTableDao()
    val snippetEmbedding = SnippetEmbeddingTableDao()
    val starSnippet = StarSnippetTableDao()
    val host = HostTableDao()
    val linkVisit = LinkVisitTableDao()
    val question = QuestionTableDao()
    val universe = UniverseTableDao()
    val signal = SignalTableDao()
    val signalScan = SignalScanTableDao()
}

//CREATE DATABASE example_db;
//CREATE USER example_user WITH PASSWORD 'hunter2';
//ALTER DATABASE example_db OWNER TO example_user;