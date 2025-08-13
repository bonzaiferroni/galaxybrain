package ponder.galaxy.server.db.services

import kabinet.utils.startOfDay
import klutch.db.DbService
import klutch.db.read
import klutch.utils.eq
import klutch.utils.greaterEq
import klutch.utils.toStringId
import klutch.utils.toUUID
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import ponder.galaxy.model.data.Idea
import ponder.galaxy.model.data.IdeaId
import ponder.galaxy.model.data.StarId
import ponder.galaxy.server.db.tables.IdeaTable
import ponder.galaxy.server.db.tables.StarIdeaTable
import ponder.galaxy.server.db.tables.StarTable
import ponder.galaxy.server.db.tables.toIdea
import ponder.galaxy.server.db.tables.writeFull
import ponder.galaxy.server.db.tables.writeUpdate

class IdeaTableDao: DbService() {

    suspend fun insert(vararg ideas: Idea) = dbQuery {
        IdeaTable.batchInsert(ideas.toList()) { writeFull(it) }
    }

    suspend fun insert(idea: Idea, starId: StarId) = dbQuery {
        IdeaTable.insert { it.writeFull(idea) }
        StarIdeaTable.insert {
            it[StarIdeaTable.starId] = starId.toUUID()
            it[StarIdeaTable.ideaId] = idea.ideaId.toUUID()
        }
    }

    suspend fun upsert(vararg ideas: Idea) = dbQuery {
        IdeaTable.batchUpsert(ideas.toList()) { writeFull(it) }
    }

    suspend fun update(vararg ideas: Idea) = dbQuery {
        ideas.forEach { idea -> IdeaTable.update { it.writeUpdate(idea) } }
    }

    suspend fun delete(vararg ideas: Idea) = dbQuery {
        IdeaTable.deleteWhere { IdeaTable.id inList ideas.map { it.ideaId.toUUID() } }
    }
    
    suspend fun readByStarId(starId: StarId) = dbQuery {
        StarTable.innerJoin(StarIdeaTable)
            .read { StarIdeaTable.ideaId.eq(starId) }.map { it.toIdea() }
    }

    suspend fun readIdeas(since: Instant? = null) = dbQuery {
        IdeaTable.innerJoin(StarIdeaTable)
            .read(IdeaTable.columns + StarIdeaTable.starId) { IdeaTable.createdAt.greaterEq(since ?: Clock.startOfDay()) }
                .groupBy({ StarId(it[StarIdeaTable.starId].value.toStringId()) }, { it.toIdea() })
    }
}