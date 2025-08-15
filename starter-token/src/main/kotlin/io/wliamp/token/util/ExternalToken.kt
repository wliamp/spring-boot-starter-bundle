package io.wliamp.token.util

import io.wliamp.token.data.OauthParty
import org.springframework.stereotype.Component

@Component
class ExternalToken(val google: OauthParty, val facebook: OauthParty, val zalo: OauthParty) {}

