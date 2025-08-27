package io.github.wliamp.token.util

import io.github.wliamp.token.data.Type
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class TokenUtilWrapper(private val tokenUtil: TokenUtil) {
    fun issue(subject: String): Mono<String> {
        return tokenUtil.issue(subject)
    }

    fun issue(subject: String, type: Type): Mono<String> {
        return tokenUtil.issue(subject, type)
    }

    fun issue(subject: String, type: Type, exp: Long): Mono<String> {
        return tokenUtil.issue(subject, type, exp)
    }

    fun issue(subject: String, type: Type, exp: Long, extraClaims: Map<String, Any>): Mono<String> {
        return tokenUtil.issue(subject, type, exp, extraClaims)
    }
}
