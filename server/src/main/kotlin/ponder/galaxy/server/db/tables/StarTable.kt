@file:OptIn(ExperimentalUuidApi::class)

package ponder.galaxy.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.utils.greaterEq
import klutch.utils.greaterEqNullable
import klutch.utils.less
import klutch.utils.lessNullable
import klutch.utils.toStringId
import klutch.utils.toUUID
import klutch.utils.toUuid
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.case
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ponder.galaxy.model.data.GalaxyId
import ponder.galaxy.model.data.Star
import ponder.galaxy.model.data.StarId
import kotlin.uuid.ExperimentalUuidApi

internal object StarTable: UUIDTable("star") {
    val galaxyId = reference("galaxy_id", GalaxyTable, onDelete = ReferenceOption.CASCADE).index()
    val url = text("permalink")
    val identifier = text("identifier").nullable()
    val title = text("title").nullable()
    val link = text("link").nullable()
    val thumbUrl = text("thumb_url").nullable()
    val imageUrl = text("image_url").nullable()
    val visibility = float("visibility").nullable()
    val commentCount = integer("comment_count").nullable()
    val voteCount = integer("vote_count").nullable()
    val wordCount = integer("word_count").nullable()
    val accessedAt = datetime("accessed_at").nullable()
    val publishedAt = datetime("published_at").nullable()
    val updatedAt = datetime("updated_at")
    val createdAt = datetime("created_at")
}

@Suppress("UNCHECKED_CAST")
internal fun StarTable.existedAt(): ExpressionWithColumnType<LocalDateTime?> =
    case()
        .When(publishedAt.isNotNull(), publishedAt)
        .Else(createdAt as ExpressionWithColumnType<LocalDateTime?>)

internal fun ResultRow.toStar() = Star(
    starId = StarId(this[StarTable.id].value.toStringId()),
    galaxyId = GalaxyId(this[StarTable.galaxyId].value.toStringId()),
    identifier = this[StarTable.identifier],
    title = this[StarTable.title],
    link = this[StarTable.link],
    url = this[StarTable.url],
    thumbUrl = this[StarTable.thumbUrl],
    imageUrl = this[StarTable.imageUrl],
    visibility = this[StarTable.visibility],
    commentCount = this[StarTable.commentCount],
    voteCount = this[StarTable.voteCount],
    wordCount = this[StarTable.wordCount],
    accessedAt = this[StarTable.accessedAt]?.toInstantFromUtc(),
    publishedAt = this[StarTable.publishedAt]?.toInstantFromUtc(),
    updatedAt = this[StarTable.updatedAt].toInstantFromUtc(),
    createdAt = this[StarTable.createdAt].toInstantFromUtc(),
)

internal fun UpdateBuilder<*>.writeFull(star: Star) {
    this[StarTable.id] = star.starId.toUUID()
    this[StarTable.galaxyId] = star.galaxyId.toUUID()
    this[StarTable.identifier] = star.identifier
    this[StarTable.createdAt] = star.createdAt.toLocalDateTimeUtc()
    this[StarTable.accessedAt] = star.accessedAt?.toLocalDateTimeUtc()
    writeUpdate(star)
}

internal fun UpdateBuilder<*>.writeUpdate(star: Star) {
    this[StarTable.title] = star.title
    this[StarTable.link] = star.link
    this[StarTable.url] = star.url
    this[StarTable.thumbUrl] = star.thumbUrl
    this[StarTable.imageUrl] = star.imageUrl
    this[StarTable.visibility] = star.visibility
    this[StarTable.commentCount] = star.commentCount
    this[StarTable.voteCount] = star.voteCount
    this[StarTable.wordCount] = star.wordCount
    this[StarTable.updatedAt] = star.updatedAt.toLocalDateTimeUtc()
    this[StarTable.publishedAt] = star.publishedAt?.toLocalDateTimeUtc()
}
