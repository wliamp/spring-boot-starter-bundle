package io.github.wliamp.pro.vrf

internal sealed class VerifyException(
    oauth: Oauth,
    message: String,
    cause: Throwable? = null
) : RuntimeException("[$oauth] $message", cause)

internal class VerifyConfigException(
    oauth: Oauth,
    message: String
) : VerifyException(oauth, message)

internal class VerifyHttpException(
    oauth: Oauth,
    status: Int,
    responseBody: String
) : VerifyException(oauth, "HTTP $status: $responseBody")

internal class VerifyParseException(
    oauth: Oauth,
    message: String,
    cause: Throwable? = null
) : VerifyException(oauth, message, cause)

internal class VerifyNetworkException(
    oauth: Oauth,
    cause: Throwable
) : VerifyException(oauth, "Network error", cause)

internal class VerifyUnexpectedException(
    oauth: Oauth,
    cause: Throwable
) : VerifyException(oauth, "Unexpected error", cause)
