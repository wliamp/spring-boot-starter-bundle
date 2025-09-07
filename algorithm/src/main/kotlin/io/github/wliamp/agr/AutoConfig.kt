package io.github.wliamp.agr

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
internal class AutoConfig {
    @Bean
    @ConditionalOnMissingBean
    fun q(): ReactiveQueue<Any> = ReactiveQueue()
}
