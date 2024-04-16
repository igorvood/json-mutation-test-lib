package ru.vood.json.mutation.lib

import arrow.core.plus
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.*
import ru.vood.json.mutation.lib.Js.json

sealed interface IMutation {

    val jsonPath: JsonPath
    val value: JsonElement


    fun <T> mutate(mutatedDto: T, serializer: KSerializer<T>, json: Json = Js.json): JsonElement =
        mutateRecursive(json.encodeToJsonElement(serializer, mutatedDto), jsonPath.value.split("/"), emptyList())

    fun mutate(mutatedJson: JsonStr, json: Json = Js.json): JsonElement =
        mutateRecursive(json.parseToJsonElement(mutatedJson.value), jsonPath.value.split("/"), emptyList())

    fun mutate(mutatedJson: JsonElement): JsonElement =
        mutateRecursive(mutatedJson, jsonPath.value.split("/"), emptyList())

    fun mutateWithType(
        name: String,
        arrayIndex: Int?,
        isLast: Boolean,
        parentNew: List<String>,
        path: List<String>,
        lastElement: JsonElement?
    ): JsonElement

    fun findNearestPathAndMutate(
        jsonElement: JsonElement?,
        path: List<String>,
        parent: List<String>,
    ): JsonElement{
        val (name, arrayIndex, isLast) = nodeProperty(path)
        val parentNew = parent.plus(arrayIndex?.let { i -> "$name[$i]" } ?: name)

        if (jsonElement == null){
            return@findNearestPathAndMutate mutateWithType(name, arrayIndex, isLast, parent, path, null)
        }

        val jsonObject = when {
            isLast && arrayIndex == null && jsonElement is JsonObject -> {
                val content = jsonElement.plus(
                    name to mutateWithType(
                        name,
                        arrayIndex,
                        isLast,
                        parentNew,
                        path,
                        jsonElement[name]
                    )
                )
                JsonObject(content)
            }
            isLast && arrayIndex != null && jsonElement is JsonObject -> {
                val content = jsonElement.plus(
                    name to mutateWithType(
                        name,
                        arrayIndex,
                        isLast,
                        parentNew,
                        path,
                        jsonElement[name]
                    )
                )
                JsonObject(content)
            }
            !isLast && arrayIndex == null && jsonElement is JsonObject -> {
                val content = jsonElement.plus(
                    name to findNearestPathAndMutate(
                        jsonElement[name],
                        path.drop(1),
                        parentNew
                    )
                )
                JsonObject(content)
            }
            !isLast && arrayIndex != null && jsonElement is JsonObject -> {
                val jsonElement1 = when (val arr = jsonElement[name]) {
                    is JsonArray -> {
                        val findNearestPathAndMutate = findNearestPathAndMutate(
                            arr.filterIndexed { index, jsonElement -> index == arrayIndex }
                                .firstOrNull(),
                            path.drop(1),
                            parentNew
                        )
                        val mapIndexed =
                            arr.mapIndexed { index, elem -> if (index != arrayIndex) elem else findNearestPathAndMutate }
                        JsonArray(mapIndexed)
                    }
                    null -> TODO("как то надо нул обрабобтать")

                    else -> error("Json element ${parentNewStr(parentNew)} not JsonArray, it has type ${arr::class.simpleName}")
                }
                JsonObject(jsonElement.plus(name to jsonElement1))


            }
            else -> error("asd")
        }

        return jsonObject
    }
    private fun mutateRecursive(jsonElement: JsonElement, path: List<String>, parent: List<String>): JsonElement {
        val (name, arrayIndex, isLast) = nodeProperty(path)
        val parentNew = parent.plus(arrayIndex?.let { i -> "$name[$i]" } ?: name)

        return when {
            isLast && arrayIndex == null && jsonElement is JsonObject -> {
                val addElement = when (this) {
                    is Delete, is Mutate -> {
                        jsonElement[name]
                            ?: error("In JsonObject not found field '${parentNewStr(parentNew)}' for $this")
                        emptyMap()
                    }

                    is Add -> {
                        require(jsonElement[name] == null) { "In JsonObject found field '${parentNewStr(parentNew)}' for $this" }
                        mapOf(name to value)
                    }
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
                            ?:error("In JsonObject not found field '${parentNewStr(parentNew)}' for $this"))
                        val mutateRecurcive = mutateRecursive(childrenJsonElement, path.drop(1), parentNew)
                        JsonObject(jsonElement.plus(name to mutateRecurcive))
                    }

                    is Mutate -> {
                        val childrenJsonElement =
                            jsonElement[name]
                                ?: error("In JsonObject not found field '${parentNewStr(parentNew)}' for $this")
                        val mutateRecurcive = mutateRecursive(childrenJsonElement, path.drop(1), parentNew)
                        JsonObject(jsonElement.plus(name to mutateRecurcive))
                    }

                    is Add -> {
                        val childrenJsonElement = jsonElement[name] ?: JsonObject(mapOf())
                        val mutateRecurcive = mutateRecursive(childrenJsonElement, path.drop(1), parentNew)
                        JsonObject(jsonElement.plus(name to mutateRecurcive))
                    }
                }
                addElement
            }

            isLast && arrayIndex != null && jsonElement is JsonObject -> {
                val childrenJsonElement =
                    jsonElement[name]
                when (childrenJsonElement) {
                    is JsonArray -> {
                        val content = when (this) {
                            is Delete -> {
                                require(arrayIndex >= 0 && arrayIndex < childrenJsonElement.size) {
                                    "Allowed range [0, ${childrenJsonElement.size - 1}] for JsonArray ${
                                        parentNewStr(
                                            parentNew
                                        )
                                    } but it not contains index $arrayIndex for $this"
                                }
                                val filterIndexed = childrenJsonElement.filterIndexed { q, _ -> q != arrayIndex }
                                val map = listOf(name to JsonArray(filterIndexed))
                                jsonElement.plus(map)
                            }

                            is Mutate -> {
                                require(arrayIndex >= 0 && arrayIndex < childrenJsonElement.size) {
                                    "Allowed range [0, ${childrenJsonElement.size - 1}] for JsonArray ${
                                        parentNewStr(
                                            parentNew
                                        )
                                    } but it not contains index $arrayIndex for $this"
                                }
                                val filterIndexed = childrenJsonElement.filterIndexed { q, _ -> q != arrayIndex }
                                val map = listOf(name to JsonArray(filterIndexed.plus(this.value)))
                                jsonElement.plus(map)
                            }

                            is Add -> {
                                require(arrayIndex >= 0 && arrayIndex == childrenJsonElement.size) {
                                    "Allowed number [${childrenJsonElement.size}] for JsonArray ${
                                        parentNewStr(
                                            parentNew
                                        )
                                    } but it equals [$arrayIndex] for $this"
                                }
                                val map = listOf(name to JsonArray(childrenJsonElement.plus(this.value)))
                                jsonElement.plus(map)
                            }
                        }
                        JsonObject(content)
                    }

                    null -> {
                        when (this) {
                            is Add -> {
                                JsonObject(jsonElement.plus(mapOf(name to JsonArray(listOf(this.value)))))
                            }

                            is Mutate, is Delete -> error("For JsonArray ${parentNewStr(parentNew)} not allowed mutation $this")
                        }
                    }

                    else -> error("Json element ${parentNewStr(parentNew)} not JsonArray, it has type ${childrenJsonElement::class.simpleName}")
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
                                mutateRecursive(e, path.drop(1), parentNew)
                            } else e
                        }
                        val map = listOf(name to JsonArray(filterIndexed))
                        val content: Map<String, JsonElement> = jsonElement1.plus(map)
                        JsonObject(content)
                    }

                    else -> error("Json element ${parentNewStr(parentNew)} not JsonArray, it has type ${childrenJsonElement::class.simpleName}")
                }

            }

            !isLast && jsonElement is JsonPrimitive -> {
                when (this) {
                    is Add, is Delete, is Mutate -> error("$this not compatible for JsonPrimitive with value $jsonElement")
                }
            }

            else -> error("Unbelivable case")

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
        fun delete(f: () -> String) = Delete(JsonPath(f()))

        infix fun String.mutateTo(jsonValue: Boolean): Mutate = Mutate(JsonPath(this), JsonPrimitive(jsonValue))

        infix fun String.mutateTo(jsonValue: Number): Mutate = Mutate(JsonPath(this), JsonPrimitive(jsonValue))

        infix fun String.mutateTo(jsonValue: String): Mutate = Mutate(JsonPath(this), JsonPrimitive(jsonValue))

        infix fun String.mutateTo(jsonValue: JsonElement): Mutate = Mutate(JsonPath(this), jsonValue)

        infix fun String.mutateTo(jsonValue: JsonStr): Mutate =
            Mutate(JsonPath(this), json.parseToJsonElement(jsonValue.value))

        infix fun String.add(jsonValue: Boolean): Add = Add(JsonPath(this), JsonPrimitive(jsonValue))

        infix fun String.add(jsonValue: Number): Add = Add(JsonPath(this), JsonPrimitive(jsonValue))

        infix fun String.add(jsonValue: String): Add = Add(JsonPath(this), JsonPrimitive(jsonValue))

        infix fun String.add(jsonValue: JsonElement): Add = Add(JsonPath(this), jsonValue)

        infix fun String.add(jsonValue: JsonStr): Add = Add(JsonPath(this), json.parseToJsonElement(jsonValue.value))
    }

}

data class Delete(
    override val jsonPath: JsonPath,
) : IMutation {

    override val value: JsonElement
        get() = JsonNull

    override fun mutateWithType(
        name: String,
        arrayIndex: Int?,
        isLast: Boolean,
        parentNew: List<String>,
        path: List<String>,
        lastElement: JsonElement?
    ): JsonElement {
        return when{
            isLast && arrayIndex == null && lastElement !=null -> value
            isLast && arrayIndex != null && lastElement !=null && lastElement is JsonArray -> {
                val elementAt = lastElement.elementAtOrNull(arrayIndex)
                if(elementAt==null){
                    error("""Allowed range [0, ${lastElement.size - 1}] for JsonArray ${parentNewStr(parentNew)} but it not contains index $arrayIndex for $this""")
                } else {
                    JsonArray(lastElement
                        .filterIndexed { index, _ -> index != arrayIndex }
                    )
                }
            }
//            !isLast ->
            else -> error("In JsonObject not found field '${parentNewStr(parentNew)}' for $this")
        }

    }
}

data class Mutate(
    override val jsonPath: JsonPath,
    override val value: JsonElement,
) : IMutation {
    override fun mutateWithType(
        name: String,
        arrayIndex: Int?,
        isLast: Boolean,
        parentNew: List<String>,
        path: List<String>,
        lastElement: JsonElement?
    ): JsonElement {
        TODO("Not yet implemented")
    }
}

data class Add(
    override val jsonPath: JsonPath,
    override val value: JsonElement,
) : IMutation {
    override fun mutateWithType(
        name: String,
        arrayIndex: Int?,
        isLast: Boolean,
        parentNew: List<String>,
        path: List<String>,
        lastElement: JsonElement?
    ): JsonElement {
        TODO("Not yet implemented")
    }
}

fun parentNewStr(parentNew: List<String>): String = parentNew.joinToString("/")