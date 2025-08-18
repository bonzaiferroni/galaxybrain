package ponder.galaxy.server.db.services

import kabinet.utils.fromEpochSecondsDouble
import kabinet.web.Url
import kabinet.web.fromHref
import klutch.web.RedditReader
import klutch.web.WebDocument
import kotlinx.datetime.Instant
import ponder.galaxy.model.data.Comment
import ponder.galaxy.model.data.CommentId
import ponder.galaxy.model.data.Snippet
import ponder.galaxy.model.data.StarId
import ponder.galaxy.model.data.StarLink
import ponder.galaxy.model.data.StarLinkId
import ponder.galaxy.model.data.StarSnippet
import ponder.galaxy.model.data.StarSnippetId
import ponder.galaxy.model.reddit.RedditCommentDto
import ponder.galaxy.server.routes.deriveVisibility

class CommentService(
    val dao: CommentTableDao = CommentTableDao(),
    private val redditReader: RedditReader = RedditReader(),
    private val snippetService: SnippetTableService = SnippetTableService(),
    private val starDao: StarTableDao = StarTableDao(),
    private val starSnippetDao: StarSnippetTableDao = StarSnippetTableDao(),
    private val starLinkDao: StarLinkTableDao = StarLinkTableDao()
) {
    suspend fun gatherComments(
        parentId: CommentId? = null,
        commentDtos: List<RedditCommentDto>,
        comments: MutableList<Comment>,
        snippetMap: MutableMap<CommentId, List<Snippet>>,
        starId: StarId,
        averageVisibility: Float,
        now: Instant,
    ) {
        val dbComments = dao.readByIdentifiers(commentDtos.map { it.id }).map { dbComment ->
            val commentDto = commentDtos.first { it.id == dbComment.identifier }
            val visibility = commentDto.deriveVisibility()
            val visibilityRatio = visibility / averageVisibility
            dbComment.copy(
                // text = commentDto.body.takeIf { it.isNotEmpty() } ?: dbComment.text,
                voteCount = commentDto.score,
                replyCount = commentDto.replies.size,
                visibility = visibility,
                visibilityRatio = visibilityRatio,
                updatedAt = now
            )
        }
        dao.update(dbComments)
        comments.addAll(dbComments)

        val newComments = commentDtos.mapNotNull { dto ->
            if (dbComments.any { it.identifier == dto.id }) return@mapNotNull null
            val visibility = dto.deriveVisibility()
            val visibilityRatio = visibility / averageVisibility
            Comment(
                commentId = CommentId.random(),
                parentId = parentId,
                starId = starId,
                identifier = dto.id,
                author = dto.author,
                depth = dto.depth,
                voteCount = dto.score,
                replyCount = dto.replies.size,
                visibility = visibility,
                visibilityRatio = visibilityRatio,
                permalink = "https://www.reddit.com${dto.permalink}",
                createdAt = Instant.fromEpochSecondsDouble(dto.createdUtc),
                updatedAt = now,
                accessedAt = now
            )
        }
        dao.insert(newComments)
        comments.addAll(newComments)

        val documents: Map<CommentId, WebDocument> = newComments.associateBy(
            keySelector = { it.commentId },
            valueTransform = { comment ->
                val dto = commentDtos.first { it.id == comment.identifier }
                val url = Url.fromHref(comment.permalink)
                redditReader.read(null, url, dto.body)
            }
        )

        val snippets: Map<CommentId, List<Snippet>> = documents.entries.associateBy(
            keySelector = { it.key },
            valueTransform = { (commentId, document) ->
                document.contents.map { content ->
                    snippetService.readOrCreateByTextAlt(content.text)
                }
            }
        )
        snippetMap.putAll(snippets)
        val dbSnippets = starSnippetDao.readByCommentIds(dbComments.map { it.commentId} )
        snippetMap.putAll(dbSnippets)

        val starSnippets: Map<CommentId, List<StarSnippet>> = snippets.entries.associateBy(
            keySelector = { it.key },
            valueTransform = { (commentId, snippets) ->
                snippets.mapIndexed { index, snippet ->
                    StarSnippet(
                        starSnippetId = StarSnippetId.random(),
                        snippetId = snippet.snippetId,
                        starId = starId,
                        commentId = commentId,
                        order = index,
                        createdAt = now,
                    )
                }
            }
        )
        starSnippetDao.insert(starSnippets.values.flatten())

        val starLinks: Map<CommentId, List<StarLink>> = documents.entries.associateBy(
            keySelector = { it.key },
            valueTransform = { (commentId, document) ->
                document.contents.flatMap { content ->
                    content.links.map { link ->
                        val toStar = starDao.readByUrl(link.url)
                        val starLinkId = StarLinkId.random()
                        val snippet = snippets.getValue(commentId).first { it.text == content.text }
                        StarLink(
                            starLinkId = starLinkId,
                            fromStarId = starId,
                            toStarId = toStar?.starId,
                            snippetId = snippet.snippetId,
                            commentId = commentId,
                            url = link.url,
                            text = link.text,
                            startIndex = link.startIndex,
                            createdAt = now
                        )
                    }
                }
            }
        )
        starLinkDao.insert(starLinks.values.flatten())

        for (commentDto in commentDtos) {
            if (commentDto.replies.isEmpty()) continue
            val comment = comments.first { it.identifier == commentDto.id }
            gatherComments(
                parentId = comment.commentId,
                commentDtos = commentDto.replies,
                comments = comments,
                snippetMap = snippetMap,
                starId = starId,
                averageVisibility = averageVisibility,
                now = now
            )
        }
    }
}