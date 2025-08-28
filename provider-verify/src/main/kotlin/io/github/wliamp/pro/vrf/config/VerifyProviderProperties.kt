package io.github.wliamp.pro.vrf.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "token")
data class VerifyProviderProperties(
    var facebook: FacebookProps = FacebookProps(),
    var google: GoogleProps = GoogleProps(),
    var zalo: ZaloProps = ZaloProps()
) {
    data class FacebookProps(
        var appId: String = "",
        var tokenInfoUrl: String = "",
        var appAccessToken: String = "",
        var infoFields: String = ""
    )

    data class GoogleProps(
        var clientId: String = "",
        var tokenInfoUrl: String = ""
    )

    data class ZaloProps(
        var appId: String = "",
        var tokenInfoUrl: String = "",
        var infoFields: String = ""
    )
}
