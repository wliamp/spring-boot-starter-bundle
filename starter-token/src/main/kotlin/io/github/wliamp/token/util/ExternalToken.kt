package io.github.wliamp.token.util

import io.wliamp.token.data.OauthParty
import org.springframework.stereotype.Component

@Component
class ExternalToken(
    val facebook: OauthParty,
    val google: OauthParty,
    val zalo: OauthParty
)


