package ru.vood.json.mutation.lib

import arrow.core.plus
import kotlinx.serialization.json.*

sealed interface IMutation {
    val jsonPath: JsonPath
    fun mutate(jsonElement: JsonElement): JsonElement
    fun find(jsonElement: JsonElement, path: List<String>): JsonElement {
        return when (jsonElement) {
            is JsonObject -> {
                val jsonElement1 = jsonElement[path[0]] ?: error("json element ${path[0]} not found ")
                find(jsonElement1, path.drop(1))
            }
            is JsonPrimitive, is JsonArray -> if (path.isEmpty()) jsonElement else {
                error("JsonPrimitive found")
            }
        }
    }

    companion object {
        fun String.delete() = Delete(JsonPath(this))

    }

}

data class Delete(
    override val jsonPath: JsonPath,
) : IMutation {


    fun mutateRecurcive(jsonElement: JsonElement, path: List<String>): JsonElement {
        val (name, arrayIndex, isLast) = if (path.isNotEmpty() && path.first().contains("[")) {
            val node = path.first()
            val indexOfBegin = node.indexOf("[")
            val indexOfEnd = node.indexOf("]")
            val name = node.substring(0, indexOfBegin)
            (name to node.substring(indexOfBegin + 1, endIndex = indexOfEnd).toInt()).plus(path.size == 1)
        } else {
            (path.first() to null).plus(path.size == 1)
        }


        return when {
            isLast && arrayIndex == null && jsonElement is JsonObject -> {
                val jsonElement1 = jsonElement[name] ?: error("In JsonObject not found field '${name}' for delete")
                val map = jsonElement.entries.map { entry ->
                    when (entry.key == name) {
                        true -> name to JsonNull
                        false -> entry.key to entry.value
                    }
                }.toMap()
                JsonObject(map)
            }
            !isLast && arrayIndex == null && jsonElement is JsonObject -> {
                val childrenJsonElement =
                    jsonElement[name] ?: error("json element ${name} not found for delete 1")
                val asdsad = mutateRecurcive(childrenJsonElement, path.drop(1))
                JsonObject(jsonElement.plus(path[0] to asdsad))
            }
            isLast && arrayIndex != null && jsonElement is JsonObject -> {
                val jsonElement1: JsonObject = jsonElement
                val childrenJsonElement =
                    jsonElement1[name] ?: error("json element ${name} not found for delete 1")
                when (childrenJsonElement) {
                    is JsonArray -> {
                        require(arrayIndex >= 0 && arrayIndex < childrenJsonElement.size) { "json element $name not contains index $arrayIndex" }
                        val filterIndexed = childrenJsonElement.filterIndexed { q, e -> q != arrayIndex }
                        val map = listOf(name to JsonArray(filterIndexed))
                        val content: Map<String, JsonElement> = jsonElement1.plus(map)
                        JsonObject(content)
                    }
                    else -> error("json element ${name} not JsonArray")
                }
            }
            !isLast && arrayIndex != null && jsonElement is JsonObject -> {
                val jsonElement1: JsonObject = jsonElement
                val childrenJsonElement =
                    jsonElement1[name] ?: error("json element ${name} not found for delete 1")
                when (childrenJsonElement) {
                    is JsonArray -> {
                        require(arrayIndex >= 0 && arrayIndex < childrenJsonElement.size) { "json element $name not contains index $arrayIndex" }
                        val filterIndexed = childrenJsonElement.mapIndexed() { q, e ->
                            if (q == arrayIndex) {
                                mutateRecurcive(e, path.drop(1))
                            } else e
                        }
                        val map = listOf(name to JsonArray(filterIndexed))
                        val content: Map<String, JsonElement> = jsonElement1.plus(map)
                        JsonObject(content)
                    }
                    else -> error("json element ${name} not JsonArray")
                }

            }


            jsonElement is JsonPrimitive || jsonElement is JsonArray -> if (path.isEmpty()) jsonElement else {
                error("JsonPrimitive found")
            }
            else -> error("name, arrayIndex, isLast")

        }


//        println(name)
//
//        val any = when (jsonElement) {
//            is JsonObject -> {
//                if (path.size == 1) {
//                    val jsonElement1 = jsonElement[name] ?: error("json element ${name} not found for delete 2")
//                    JsonObject(mapOf(name to JsonNull))
//                } else {
//                    val childrenJsonElement =
//                        jsonElement[name] ?: error("json element ${name} not found for delete 1")
//                    val asdsad = asdsad(childrenJsonElement, path.drop(1))
//                    JsonObject(jsonElement.plus(path[0] to asdsad))
//                }
//            }
//            is JsonPrimitive, is JsonArray -> if (path.isEmpty()) jsonElement else {
//                error("JsonPrimitive found")
//            }
//        }
//        return any
    }

    override fun mutate(jsonElement: JsonElement): JsonElement {


        return mutateRecurcive(jsonElement, jsonPath.value.split("/"))
    }
}

data class Add(
    override val jsonPath: JsonPath,
    val value: JsonElement,
) : IMutation {
    override fun mutate(jsonElement: JsonElement): JsonElement {
        TODO("Not yet implemented")
    }
}

data class Change(
    override val jsonPath: JsonPath,
    val value: JsonElement,
) : IMutation {
    override fun mutate(jsonElement: JsonElement): JsonElement {
        TODO("Not yet implemented")
    }
}

