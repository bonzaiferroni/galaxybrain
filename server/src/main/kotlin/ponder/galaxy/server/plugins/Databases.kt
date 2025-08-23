package ponder.galaxy.server.plugins

import io.ktor.server.application.*
import klutch.db.initDb
import klutch.db.tables.RefreshTokenTable
import klutch.db.tables.UserTable
import klutch.environment.readEnvFromPath
import ponder.galaxy.model.data.LinkVisit
import ponder.galaxy.server.db.tables.CommentTable
import ponder.galaxy.server.db.tables.GalaxyTable
import ponder.galaxy.server.db.tables.IdeaTable
import ponder.galaxy.server.db.tables.StarLinkTable
import ponder.galaxy.server.db.tables.StarLogTable
import ponder.galaxy.server.db.tables.StarTable
import ponder.galaxy.server.db.tables.StarSnippetTable
import ponder.galaxy.server.db.tables.HostTable
import ponder.galaxy.server.db.tables.LinkVisitTable
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
)

//CREATE DATABASE example_db;
//CREATE USER example_user WITH PASSWORD 'hunter2';
//ALTER DATABASE example_db OWNER TO example_user;