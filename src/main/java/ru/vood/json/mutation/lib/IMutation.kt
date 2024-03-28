package ru.vood.json.mutation.lib

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

    override fun mutate(jsonElement: JsonElement): JsonElement {
        fun asdsad(jsonElement: JsonElement, path: List<String>): JsonElement {
            val any = when (jsonElement) {
                is JsonObject -> {
                    if (path.size == 1) {
                        val jsonElement1 = jsonElement[path[0]] ?: error("json element ${path[0]} not found for delete")
                        JsonObject(mapOf(path[0] to JsonNull))
                    } else {
                        val childrenJsonElement =
                            jsonElement[path[0]] ?: error("json element ${path[0]} not found for delete")
                        val asdsad = asdsad(childrenJsonElement, path.drop(1))
                        JsonObject(jsonElement.plus(path[0] to asdsad))
                    }
                }
                is JsonPrimitive, is JsonArray -> if (path.isEmpty()) jsonElement else {
                    error("JsonPrimitive found")
                }
            }
            return any
        }

        return asdsad(jsonElement, jsonPath.value.split("/"))
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

