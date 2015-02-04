import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy

import static ch.qos.logback.classic.Level.*

scan()

appender("CONSOLE", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "%d{dd-MM-yyyy HH:mm:ss.SSS} %p [%t] %c{1}: %m%n"
  }
}

appender("CRAWLERS", RollingFileAppender) {
  file = "crawlers.log"
  rollingPolicy(FixedWindowRollingPolicy) {
    fileNamePattern = "crawlers_%i.log"
    minIndex = 1
    maxIndex = 24
  }
  triggeringPolicy(SizeBasedTriggeringPolicy) {
    maxFileSize = "12MB"
  }
  encoder(PatternLayoutEncoder) {
    pattern = "%d{dd-MM-yyyy HH:mm:ss.SSS} %p [%t] %c{1}: %m%n"
  }
}


appender("ENRICHER", RollingFileAppender) {
  file = "enricher.log"
  rollingPolicy(FixedWindowRollingPolicy) {
    fileNamePattern = "enricher_%i.log"
    minIndex = 1
    maxIndex = 24
  }
  triggeringPolicy(SizeBasedTriggeringPolicy) {
    maxFileSize = "12MB"
  }
  encoder(PatternLayoutEncoder) {
    pattern = "%d{dd-MM-yyyy HH:mm:ss.SSS} %p [%t] %c{1}: %m%n"
  }
}

appender("IMPORT", RollingFileAppender) {
  file = "import.log"
  rollingPolicy(FixedWindowRollingPolicy) {
    fileNamePattern = "import_%i.log"
    minIndex = 1
    maxIndex = 24
  }
  triggeringPolicy(SizeBasedTriggeringPolicy) {
    maxFileSize = "12MB"
  }
  encoder(PatternLayoutEncoder) {
    pattern = "%d{dd-MM-yyyy HH:mm:ss.SSS} %p [%t] %c{1}: %m%n"
  }
}

appender("PROPERTIES", RollingFileAppender) {
  file = "properties.log"
  rollingPolicy(FixedWindowRollingPolicy) {
    fileNamePattern = "properties_%i.log"
    minIndex = 1
    maxIndex = 24
  }
  triggeringPolicy(SizeBasedTriggeringPolicy) {
    maxFileSize = "12MB"
  }
  encoder(PatternLayoutEncoder) {
    pattern = "%d{dd-MM-yyyy HH:mm:ss.SSS} %p [%t] %c{1}: %m%n"
  }
}

appender("ROOT", RollingFileAppender) {
  file = "root.log"
  rollingPolicy(FixedWindowRollingPolicy) {
    fileNamePattern = "root_%i.log"
    minIndex = 1
    maxIndex = 24
  }
  triggeringPolicy(SizeBasedTriggeringPolicy) {
    maxFileSize = "12MB"
  }
  encoder(PatternLayoutEncoder) {
    pattern = "%d{dd-MM-yyyy HH:mm:ss.SSS} %p [%t] %c{1}: %m%n"
  }
}

logger("com.techlooper.crawlers.GitHubUserCrawler", ALL, ["CONSOLE", "CRAWLERS"], Boolean.FALSE)
logger("com.techlooper.imports.GitHubUserProfileEnricher", ALL, ["CONSOLE","ENRICHER"], Boolean.FALSE)
logger("com.techlooper.imports.GitHubUserImport", ALL, ["CONSOLE", "IMPORT"], Boolean.FALSE)
logger("com.techlooper.utils.PropertyManager", ALL, ["CONSOLE", "PROPERTIES"], Boolean.FALSE)

root(ALL, ["CONSOLE", "ROOT"])