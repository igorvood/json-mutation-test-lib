package ru.vood.json.mutation.lib

import arrow.core.plus
import kotlinx.serialization.json.*

sealed interface IMutation {
    val jsonPath: JsonPath
    val value: JsonElement
    fun mutate(mutatedJson: JsonElement): JsonElement = mutateRecursive(mutatedJson, jsonPath.value.split("/"))
    fun mutateRecursive(jsonElement: JsonElement, path: List<String>): JsonElement {
        val (name, arrayIndex, isLast) = nodeProperty(path)


        return when {
            isLast && arrayIndex == null && jsonElement is JsonObject -> {
                val addElement = when (this) {
                    is Delete -> {
                        jsonElement[name]
                            ?: error("In JsonObject not found field '${name}' for ${Delete::class.simpleName}")
                        emptyMap()
                    }
                    is Mutate -> mapOf(name to value)
                }
                val map = jsonElement.entries.map { entry ->
                    when (entry.key == name) {
                        true -> name to value
                        false -> entry.key to entry.value
                    }
                }.toMap().plus(addElement)
                JsonObject(map)
            }
            !isLast && arrayIndex == null && jsonElement is JsonObject -> {
                val addElement = when (this) {
                    is Delete -> {
                        val childrenJsonElement = (jsonElement[name]
                            ?: error("In JsonObject not found field '${name}' for ${Delete::class.simpleName}"))
                        val mutateRecurcive = mutateRecursive(childrenJsonElement, path.drop(1))
                        JsonObject(jsonElement.plus(name to mutateRecurcive))
                    }
                    is Mutate -> {
                        val childrenJsonElement = jsonElement[name]?:JsonObject(mapOf())
                        val mutateRecurcive = mutateRecursive(childrenJsonElement, path.drop(1))

                        JsonObject(jsonElement.plus(name to mutateRecurcive))
                    }
                }
                addElement
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
                                mutateRecursive(e, path.drop(1))
                            } else e
                        }
                        val map = listOf(name to JsonArray(filterIndexed))
                        val content: Map<String, JsonElement> = jsonElement1.plus(map)
                        JsonObject(content)
                    }
                    else -> error("json element ${name} not JsonArray")
                }

            }
            !isLast && jsonElement is JsonPrimitive -> {
                when(this){
                    is Mutate -> error("Unable add new object to JsonPrimitive")
                    is Delete -> error("Delete not compatible")
                }
            }

            jsonElement is JsonPrimitive || jsonElement is JsonArray -> if (path.isEmpty()) jsonElement else {
                error("JsonPrimitive found")
            }
            else -> error("name, arrayIndex, isLast")

        }
    }

    fun nodeProperty(path: List<String>) = if (path.isNotEmpty() && path.first().contains("[")) {
        val node = path.first()
        val indexOfBegin = node.indexOf("[")
        val indexOfEnd = node.indexOf("]")
        val name = node.substring(0, indexOfBegin)
        (name to node.substring(indexOfBegin + 1, endIndex = indexOfEnd).toInt()).plus(path.size == 1)
    } else {
        (path.first() to null).plus(path.size == 1)
    }

    companion object {
        fun String.delete() = Delete(JsonPath(this))


        infix fun String.mutateToValue(jsonValue: Boolean?): Mutate = Mutate(JsonPath(this),JsonPrimitive(jsonValue))


        infix fun String.mutateToValue(jsonValue: Number?): Mutate = Mutate(JsonPath(this),JsonPrimitive(jsonValue))

        infix fun String.mutateToValue(jsonValue: String?): Mutate = Mutate(JsonPath(this),JsonPrimitive(jsonValue))


    }

}

data class Delete(
    override val jsonPath: JsonPath,
) : IMutation {

    override val value: JsonElement
        get() = JsonNull

}

data class Mutate(
    override val jsonPath: JsonPath,
    override val value: JsonElement,
) : IMutation {

}

//data class Change(
//    override val jsonPath: JsonPath,
//    val value: JsonElement,
//) : IMutation {
//    override fun mutate(mutatedJson: JsonElement): JsonElement {
//        TODO("Not yet implemented")
//    }
//}

