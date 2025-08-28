package io.github.wliamp.pro.vrf

import org.springframework.stereotype.Component

@Component
class OauthProvider(
    val facebook: IOauth,
    val google: IOauth,
    val zalo: IOauth
)
