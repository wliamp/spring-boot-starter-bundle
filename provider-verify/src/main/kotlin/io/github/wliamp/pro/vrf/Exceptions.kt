package io.github.wliamp.pro.vrf

internal sealed class VerifyException(
    provider: String,
    message: String,
    cause: Throwable? = null
) : RuntimeException("[$provider] $message", cause)

internal class VerifyConfigException(
    provider: String,
    message: String
) : VerifyException(provider, message)

internal class VerifyHttpException(
    provider: String,
    status: Int,
    responseBody: String
) : VerifyException(provider, "HTTP $status: $responseBody")

internal class VerifyParseException(
    provider: String,
    message: String,
    cause: Throwable? = null
) : VerifyException(provider, message, cause)

internal class VerifyNetworkException(
    provider: String,
    cause: Throwable
) : VerifyException(provider, "Network error", cause)

internal class VerifyUnexpectedException(
    provider: String,
    cause: Throwable
) : VerifyException(provider, "Unexpected error", cause)
