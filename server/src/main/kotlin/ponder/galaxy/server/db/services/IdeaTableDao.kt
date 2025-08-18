@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.services

import kabinet.utils.startOfDay
import klutch.db.DbService
import klutch.db.read
import klutch.db.readFirstOrNull
import klutch.utils.greaterEq
import klutch.utils.toUUID
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
import ponder.galaxy.model.data.Idea
import ponder.galaxy.model.data.StarId
import ponder.galaxy.server.db.tables.IdeaTable
import ponder.galaxy.server.db.tables.toIdea
import ponder.galaxy.server.db.tables.writeFull
import ponder.galaxy.server.db.tables.writeUpdate
import klutch.utils.eq
import org.jetbrains.exposed.sql.and
import ponder.galaxy.model.data.CommentId
import kotlin.uuid.ExperimentalUuidApi

class IdeaTableDao: DbService() {

    suspend fun insert(vararg ideas: Idea) = dbQuery {
        IdeaTable.batchInsert(ideas.toList()) { writeFull(it) }
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
    
    suspend fun readIdeasByStarId(starId: StarId) = dbQuery {
        IdeaTable.read { it.starId.eq(starId) }.map { it.toIdea() }
    }

    suspend fun readHeadlineByStarId(starId: StarId) = dbQuery {
        IdeaTable.readFirstOrNull { it.starId.eq(starId) and it.description.eq(IDEA_HEADLINE_DESCRIPTION) }?.toIdea()
    }

    suspend fun readIdeas(since: Instant? = null) = dbQuery {
        IdeaTable.read { it.createdAt.greaterEq(since ?: Clock.startOfDay()) }.map { it.toIdea() }
    }

    suspend fun readIdeas(starId: StarId, description: String) = dbQuery {
        IdeaTable.read { it.starId.eq(starId) and it.description.eq(description) }.map { it.toIdea() }
    }

    suspend fun readIdeas(commentId: CommentId) = dbQuery {
        IdeaTable.read { it.commentId.eq(commentId) }.map { it.toIdea() }
    }
}