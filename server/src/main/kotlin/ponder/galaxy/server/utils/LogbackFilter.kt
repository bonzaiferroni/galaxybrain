package ponder.galaxy.server.utils

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply

class LogbackFilter : Filter<ILoggingEvent>() {
    override fun decide(event: ILoggingEvent): FilterReply {
        val throwable = event.throwableProxy ?: return FilterReply.NEUTRAL
        val className = throwable.className
        if (deniedMessages.contains(throwable.message)) {
            println("eyyy")
        }
        return if (deniedClassnames.contains(className))
            FilterReply.DENY
        else
            FilterReply.NEUTRAL
    }
}

val deniedClassnames = setOf(
    "io.ktor.utils.io.ClosedByteChannelException",
    "java.nio.channels.ClosedChannelException",
    // "java.net.SocketException"
)

val deniedMessages = setOf(
    "java.net.SocketException: Connection reset"
)