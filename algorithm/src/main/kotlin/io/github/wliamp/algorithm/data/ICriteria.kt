package io.github.wliamp.algorithm.data

interface ICriteria {
    fun matches(other: ICriteria): Boolean
}
