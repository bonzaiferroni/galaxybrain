package ponder.galaxy.server.plugins

import io.ktor.server.application.*
import klutch.db.initDb
import klutch.db.tables.RefreshTokenTable
import klutch.db.tables.UserTable
import klutch.environment.readEnvFromPath
import ponder.galaxy.server.db.tables.GalaxyTable
import ponder.galaxy.server.db.tables.StarLogTable
import ponder.galaxy.server.db.tables.StarTable

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
)

//CREATE DATABASE example_db;
//CREATE USER example_user WITH PASSWORD 'hunter2';
//ALTER DATABASE example_db OWNER TO example_user;