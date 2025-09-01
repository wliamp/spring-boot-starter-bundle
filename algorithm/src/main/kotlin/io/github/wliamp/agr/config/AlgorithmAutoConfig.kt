package io.github.wliamp.agr.config

import io.github.wliamp.agr.Matcher
import io.github.wliamp.agr.exe.QueueExecute
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
class AlgorithmAutoConfig {
    @Bean
    @ConditionalOnMissingBean
    fun matchMaker(queue: QueueExecute): Matcher = Matcher(queue)
}
