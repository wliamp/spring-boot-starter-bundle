package io.github.wliamp.provider.util

import io.github.wliamp.provider.data.Oauth
import org.springframework.stereotype.Component

@Component
class OauthVerifier(
    val facebook: Oauth,
    val google: Oauth,
    val zalo: Oauth
)


