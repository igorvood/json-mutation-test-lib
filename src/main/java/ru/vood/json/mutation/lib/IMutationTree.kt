package ru.vood.json.mutation.lib

sealed interface IMutationTree

object NullMutationTree: IMutationTree

data class MT(val asd:String): IMutationTree