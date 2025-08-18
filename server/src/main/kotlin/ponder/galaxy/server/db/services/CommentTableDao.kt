@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.services

import klutch.db.DbService
import klutch.db.batchUpdate
import klutch.db.read
import klutch.db.readByIdOrNull
import klutch.db.readSingleOrNull
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.statements.BatchUpdateStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.update
import ponder.galaxy.model.data.Comment
import ponder.galaxy.model.data.CommentId
import ponder.galaxy.server.db.tables.CommentTable
import ponder.galaxy.server.db.tables.toComment
import ponder.galaxy.server.db.tables.writeFull
import ponder.galaxy.server.db.tables.writeUpdate
import kotlin.uuid.ExperimentalUuidApi

class CommentTableDao : DbService() {

    suspend fun insert(comments: List<Comment>) = dbQuery {
        CommentTable.batchInsert(comments) { writeFull(it) }
    }

    suspend fun upsert(vararg comments: Comment) = dbQuery {
        CommentTable.batchUpsert(comments.toList()) { writeFull(it) }
    }

    suspend fun update(comment: Comment) = dbQuery {
        CommentTable.update { it.writeUpdate(comment) }
    }

    suspend fun update(comments: List<Comment>) = dbQuery {
        CommentTable.batchUpdate(comments, { it.commentId.toUUID() }) { writeUpdate(it) }
    }

    suspend fun updateOld(comments: List<Comment>) = dbQuery {
        if (comments.isEmpty()) return@dbQuery 0
        var total = 0
        BatchUpdateStatement(CommentTable).apply {
            comments.forEach { comment ->
                addBatch(EntityID(comment.commentId.toUUID(), CommentTable))
                this.writeUpdate(comment)
            }
            total = execute(TransactionManager.current()) ?: 0
        }
        total
    }

    suspend fun delete(vararg comments: Comment) = dbQuery {
        CommentTable.deleteWhere { CommentTable.id inList comments.map { it.commentId.toUUID() } }
    }

    suspend fun readByIdOrNull(commentId: CommentId) = dbQuery {
        CommentTable.readByIdOrNull(commentId.toUUID())?.toComment()
    }

    suspend fun readByIdentifier(identifier: String) = dbQuery {
        CommentTable.readSingleOrNull { it.identifier eq identifier }?.toComment()
    }

    suspend fun readByIdentifiers(identifiers: List<String>) = dbQuery {
        CommentTable.read { it.identifier.inList(identifiers) }.map {it.toComment() }
    }

    suspend fun readByIds(commentIds: List<CommentId>) = dbQuery {
        CommentTable.read { it.id.inList(commentIds.map { id -> id.toUUID() }) }.map { it.toComment() }
    }
}
