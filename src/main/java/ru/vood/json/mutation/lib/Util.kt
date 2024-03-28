package ru.vood.json.mutation.lib

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

object Util {

    fun Map<JsonPath, IMutation>.toTree() {
        val map = this.entries
            .map { entry ->
                val split = entry.key.value.split("/")
                split.first() to (split.drop(1) to entry.value)
            }
            .groupBy { it.first }
            .map { entry ->
                val map = entry.value.map { asd -> asd.second }
                val pair = entry.key to map
                pair
            }

        EmptyMutationTree.add()


    }
//
//    infix fun JsonElement.mutate(mutationRule: IMutation): JsonObject {
//
//        when(mutationRule){
//            is Delete -> ""
//            is Add -> ""
//            is Change -> ""
//        }
//
//
//
//        fun recurcive(jsonElement: JsonElement, mutationRule: IMutation, split: List<String>):JsonObject{
//            when(split.size==1){
//                true -> when(mutationRule){
//                    is Delete -> ""
//                    is Add -> ""
//                    is Change -> ""
//                }
//                false -> {
//                    if (jsonElement is JsonObject){
//                        val jsonObject1 = jsonElement[split[0]] ?: error("")
//                        recurcive(jsonObject1, mutationRule, split.drop(1))
//                    }
//                    else{
//
//                    }
//
//
//
//                }
//            }
//        }
//
//       return recurcive(this, mutationRule, mutationRule.jsonPath.value.split("/"))
//
//
//    }


    private infix fun String.mutate(jsonPrimitive: JsonPrimitive): JsonObject {
        val split = this.split("/")

        fun addRecurcive(split: List<String>): JsonObject {
            val jsonObject = when (split.size == 1) {
                true -> JsonObject(mapOf(split[0] to jsonPrimitive))
                false -> JsonObject(mapOf(split[0] to addRecurcive(split.drop(1))))
            }
            return jsonObject
        }

        addRecurcive(split)

        return addRecurcive(split)
    }

    infix fun String.mutate(jsonValue: Boolean?): JsonObject =
        this mutate JsonPrimitive(jsonValue)

    infix fun String.mutate(jsonValue: Number?): JsonObject =
        this mutate JsonPrimitive(jsonValue)

    infix fun String.mutate(jsonValue: String?): JsonObject =
        this mutate JsonPrimitive(jsonValue)

    fun String.mutate(): JsonObject =
        this mutate JsonNull

}