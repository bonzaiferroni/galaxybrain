package ponder.galaxy.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextDecoration
import ponder.galaxy.model.data.StarLink
import pondui.ui.theme.Pond
import pondui.utils.darken
import pondui.utils.lighten

@Composable
fun SnippetText(
    text: String,
    starLinks: List<StarLink>,
    modifier: Modifier = Modifier,
    onLinkClick: (StarLink) -> Unit
) {
    val colors = Pond.colors
    val localColors = Pond.localColors
    val linkColor = colors.action.lighten(.3f)
    val annotated = remember(text, starLinks) {
        val b = AnnotatedString.Builder(text)

        starLinks
            .filter { it.text != null && it.startIndex != null }
            .sortedBy { it.startIndex!! }
            .forEach { link ->
                val start = link.startIndex!!
                val linkText = link.text!!
                val end = start + linkText.length

                if (start >= 0 && end <= text.length &&
                    text.regionMatches(start, linkText, 0, linkText.length)
                ) {
                    b.addStyle(
                        SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
                        start,
                        end
                    )
                    b.addLink(
                        LinkAnnotation.Clickable(
                            tag = link.starLinkId.toString(),
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = linkColor,
                                    textDecoration = TextDecoration.Underline
                                ),
                                hoveredStyle = SpanStyle(
                                    color = linkColor.lighten(.1f), // darker blue on hover
                                    textDecoration = TextDecoration.Underline
                                ),
                                pressedStyle = SpanStyle(
                                    color = linkColor.darken(.1f), // even darker when pressed
                                    textDecoration = TextDecoration.Underline
                                )
                            ),
                            linkInteractionListener = {
                                onLinkClick(link)
                            }
                        ),
                        start,
                        end
                    )
                }
            }

        b.toAnnotatedString()
    }

    BasicText(
        text = annotated,
        modifier = modifier,
        color = {localColors.content}
    )
}