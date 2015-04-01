import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy

import static ch.qos.logback.classic.Level.ALL
import static ch.qos.logback.classic.Level.ERROR

scan()

appender("CONSOLE", ConsoleAppender) {
    withJansi = true
    encoder(PatternLayoutEncoder) {
//    pattern = "%d{dd-MM-yyyy HH:mm:ss.SSS} %p [%t] %c{1}: %m%n"
        pattern = "[%t] %d{yyyyMMdd HH:mm:ss.SSS} %highlight(%-5level) %cyan(%logger{15}) - %msg %n"
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
logger("com.techlooper.enricher", ALL, ["CONSOLE", "ENRICHER"], Boolean.FALSE)


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

appender("VIETNAMWORKS_IMPORT", RollingFileAppender) {
    file = "vietnamworks_import.log"
    rollingPolicy(FixedWindowRollingPolicy) {
        fileNamePattern = "vietnamworks_import_%i.log"
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

appender("GITHUB_AWARD_ENRICH", RollingFileAppender) {
    file = "github_award_enrich.log"
    rollingPolicy(FixedWindowRollingPolicy) {
        fileNamePattern = "github_award_enrich_%i.log"
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

appender("VNW_COMPANY_IMPORT", RollingFileAppender) {
    file = "VNW_COMPANY_IMPORT.log"
    rollingPolicy(FixedWindowRollingPolicy) {
        fileNamePattern = "VNW_COMPANY_IMPORT_%i.log"
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
logger("com.techlooper.imports.GitHubUserProfileEnricher", ALL, ["CONSOLE", "ENRICHER"], Boolean.FALSE)
logger("com.techlooper.imports.GitHubUserImport", ALL, ["CONSOLE", "IMPORT"], Boolean.FALSE)
logger("com.techlooper.utils.PropertyManager", ALL, ["CONSOLE", "PROPERTIES"], Boolean.FALSE)
logger("com.techlooper.imports.VietnamworksUserImport", ALL, ["CONSOLE", "VIETNAMWORKS_IMPORT"], Boolean.FALSE)
logger("com.techlooper.crawlers.GithubAwardUserCrawler", ALL, ["CONSOLE", "GITHUB_AWARD_ENRICH"], Boolean.FALSE)
logger("com.techlooper.service.GithubAwardUserService", ALL, ["CONSOLE", "GITHUB_AWARD_ENRICH"], Boolean.FALSE)
logger("com.techlooper.imports.CompanyProfileImport", ALL, ["CONSOLE", "VNW_COMPANY_IMPORT"], Boolean.FALSE)
logger("com.techlooper.service.CompanyService", ALL, ["CONSOLE", "VNW_COMPANY_IMPORT"], Boolean.FALSE)

root(ERROR, ["CONSOLE", "ROOT"])