package io.github.wliamp.pro.vrf.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "provider.oauth")
internal data class VerifyProviderProperties(
    var facebook: FacebookProps = FacebookProps(),
    var google: GoogleProps = GoogleProps(),
    var zalo: ZaloProps = ZaloProps()
) {
    data class FacebookProps(
        var baseUrl: String = "https://graph.facebook.com",
        var appId: String = "",
        var accessToken: String = "",
        var fields: String = ""
    )

    data class GoogleProps(
        var baseUrl: String = "https://oauth2.googleapis.com/tokeninfo",
        var clientId: String = ""
    )

    data class ZaloProps(
        var baseUrl: String = "https://graph.zalo.me/v2.0/me",
        var fields: String = ""
    )
}
