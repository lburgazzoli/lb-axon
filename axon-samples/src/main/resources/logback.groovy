import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

import static ch.qos.logback.classic.Level.DEBUG

appender("STDOUT", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "%d %contextName [%t] %level %logger{36} - %msg%n"
  }
}

logger("com.hazelcast", INFO)
logger("org.springframework", INFO)

root(DEBUG, ["STDOUT"])

