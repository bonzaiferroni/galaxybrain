package ponder.galaxy.server.db.services

import klutch.db.DbService

class StarService(
    val dao: StarTableDao = StarTableDao()
): DbService() {


}