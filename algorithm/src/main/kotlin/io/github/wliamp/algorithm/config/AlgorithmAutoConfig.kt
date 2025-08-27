package io.github.wliamp.algorithm.config

import io.github.wliamp.algorithm.util.MatchMaker
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
class AlgorithmAutoConfig {
    @Bean
    @ConditionalOnMissingBean
    fun matchMaker(): MatchMaker = MatchMaker()
}
