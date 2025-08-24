package io.github.wliamp.token.util

import io.wliamp.token.data.Type
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class InternalTokenWrapper(private val internalToken: InternalToken) {
    fun issue(subject: String): Mono<String> {
        return internalToken.issue(subject)
    }

    fun issue(subject: String, type: Type): Mono<String> {
        return internalToken.issue(subject, type)
    }

    fun issue(subject: String, type: Type, exp: Long): Mono<String> {
        return internalToken.issue(subject, type, exp)
    }

    fun issue(subject: String, type: Type, exp: Long, extraClaims: Map<String, Any>): Mono<String> {
        return internalToken.issue(subject, type, exp, extraClaims)
    }
}
