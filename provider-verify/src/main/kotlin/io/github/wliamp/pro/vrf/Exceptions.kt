package io.github.wliamp.pro.vrf

internal sealed class GoogleOauthException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

internal class GoogleConfigException(message: String) : GoogleOauthException(message)

internal class GoogleHttpException(
    status: Int,
    responseBody: String
) : GoogleOauthException("HTTP $status: $responseBody")

internal class GoogleParseException(
    message: String,
    cause: Throwable? = null
) : GoogleOauthException(message, cause)

internal class GoogleNetworkException(
    cause: Throwable
) : GoogleOauthException("Network error", cause)

internal class GoogleUnexpectedException(
    cause: Throwable
) : GoogleOauthException("Unexpected error", cause)
