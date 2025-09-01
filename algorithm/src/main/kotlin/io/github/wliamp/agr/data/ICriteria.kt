package io.github.wliamp.agr.data

interface ICriteria {
    fun matches(other: ICriteria): Boolean
}
