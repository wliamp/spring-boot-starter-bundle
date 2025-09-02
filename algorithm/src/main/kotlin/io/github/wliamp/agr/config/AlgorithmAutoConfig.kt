package io.github.wliamp.agr.config

import io.github.wliamp.agr.data.ReactiveQueue
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
internal class AlgorithmAutoConfig {
    @Bean
    @ConditionalOnMissingBean
    fun q(): ReactiveQueue<Any> = ReactiveQueue()
}
