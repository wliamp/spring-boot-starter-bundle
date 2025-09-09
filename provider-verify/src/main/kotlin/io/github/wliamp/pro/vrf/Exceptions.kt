package io.github.wliamp.pro.vrf

internal sealed class OauthException(
    oauth: Oauth,
    message: String,
    cause: Throwable? = null
) : RuntimeException("[$oauth] $message", cause)

internal class OauthConfigException(
    oauth: Oauth,
    message: String
) : OauthException(oauth, message)

internal class OauthHttpException(
    oauth: Oauth,
    status: Int,
    responseBody: String
) : OauthException(oauth, "HTTP $status: $responseBody")

internal class OauthParseException(
    oauth: Oauth,
    message: String,
    cause: Throwable? = null
) : OauthException(oauth, message, cause)

internal class OauthNetworkException(
    oauth: Oauth,
    cause: Throwable
) : OauthException(oauth, "Network error", cause)

internal class OauthUnexpectedException(
    oauth: Oauth,
    cause: Throwable
) : OauthException(oauth, "Unexpected error", cause)
