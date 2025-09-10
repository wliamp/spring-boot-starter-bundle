package io.github.wliamp.pro.vrf

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "provider.otp")
internal data class OtpProps(
    var firebase: FirebaseProps = FirebaseProps(),
) {
    data class FirebaseProps(
        var baseUrl: String = "https://identitytoolkit.googleapis.com",
        var version: String = "/v1",
        var uri: String = "/accounts:signInWithPhoneNumber",
        var apiKey: String = ""
    )
}
