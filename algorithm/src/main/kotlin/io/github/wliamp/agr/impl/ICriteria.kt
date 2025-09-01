package io.github.wliamp.agr.impl

interface ICriteria {
    fun matches(other: ICriteria): Boolean
}
