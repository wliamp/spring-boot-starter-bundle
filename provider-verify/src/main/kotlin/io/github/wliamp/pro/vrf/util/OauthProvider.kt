package io.github.wliamp.pro.vrf.util

import io.github.wliamp.pro.vrf.data.IOauth
import org.springframework.stereotype.Component

@Component
class OauthProvider(
    val facebook: IOauth,
    val google: IOauth,
    val zalo: IOauth
)


