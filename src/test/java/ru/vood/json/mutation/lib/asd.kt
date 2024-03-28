package ru.vood.json.mutation.lib

import kotlinx.serialization.Serializable

@Serializable
data class A1(val a2: A2)
@Serializable
data class A2(val a3: A3)

@Serializable
data class A3(val a4: List<A4>)

@Serializable
data class A4(val f1: String, val f2: String)