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

data class MT(
    override val name: String,
    override val mutation: List<IMutation>,
    override val children: List<IMutationTree>,
) : IMutationTree {

    init {
       require(mutation.isNotEmpty() || children.isNotEmpty())
    }

    override fun add(): IMutationTree {
        TODO("Not yet implemented")
    }
}