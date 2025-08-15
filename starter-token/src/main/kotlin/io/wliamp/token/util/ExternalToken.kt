package io.wliamp.token.util

import io.wliamp.token.data.OauthParty

class ExternalToken private constructor() {
    companion object {
        lateinit var GOOGLE: OauthParty
        lateinit var FACEBOOK: OauthParty
        lateinit var ZALO: OauthParty
    }
}
