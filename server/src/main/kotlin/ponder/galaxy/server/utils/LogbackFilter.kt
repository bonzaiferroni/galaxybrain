package ponder.galaxy.server.utils

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply

class LogbackFilter : Filter<ILoggingEvent>() {
    override fun decide(event: ILoggingEvent): FilterReply {
        val t = event.throwableProxy ?: return FilterReply.NEUTRAL
        val ex = t.className
        return if (ex == "io.ktor.utils.io.ClosedByteChannelException" ||
            ex == "java.nio.channels.ClosedChannelException")
            FilterReply.DENY
        else
            FilterReply.NEUTRAL
    }
}