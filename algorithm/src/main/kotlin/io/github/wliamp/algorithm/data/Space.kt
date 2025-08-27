package io.github.wliamp.algorithm.data

data class Space(
    val id: String,
    val minJoins: Int,
    val maxJoins: Int,
    private val _targets: MutableList<Target> = mutableListOf()
) {
    val targets: List<Target> get() = _targets // expose immutable view

    fun canJoin(target: Target): Boolean =
        _targets.size < maxJoins &&
            (_targets.isEmpty() || _targets.first().criteria.matches(target.criteria))

    fun addTarget(target: Target): Boolean =
        canJoin(target).also { if (it) _targets += target }
}
