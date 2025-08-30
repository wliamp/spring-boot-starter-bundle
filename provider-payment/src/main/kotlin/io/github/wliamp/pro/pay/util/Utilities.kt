package io.github.wliamp.pro.pay.util

internal fun MutableMap<String, String>.putIfNotBlank(key: String, value: String?) {
    when {
        !value.isNullOrBlank() -> this[key] = value
    }
}
