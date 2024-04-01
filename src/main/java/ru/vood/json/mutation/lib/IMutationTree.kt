package ru.vood.json.mutation.lib

sealed interface IMutationTree {

    val name: String
    val mutation: List<IMutation>
    val children: List<IMutationTree>

    fun add(): IMutationTree
}

object EmptyMutationTree: IMutationTree{
    override val name: String
        get() = "root"
    override val mutation: List<IMutation>
        get() = emptyList()
    override val children: List<IMutationTree>
        get() = emptyList()

    override fun add(): IMutationTree {
        TODO("Not yet implemented")
    }
}

