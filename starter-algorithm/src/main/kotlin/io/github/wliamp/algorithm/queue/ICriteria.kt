package io.github.wliamp.algorithm.queue

interface ICriteria {
    fun matches(other: ICriteria): Boolean
}
