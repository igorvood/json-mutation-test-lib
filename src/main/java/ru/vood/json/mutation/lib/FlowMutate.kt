package ru.vood.json.mutation.lib

import kotlinx.serialization.json.JsonElement
import ru.vood.json.mutation.lib.Js.json

infix fun JsonStr.withRules(mutationRules: List<IMutation>): JsonElement =
    json.parseToJsonElement(this.value) withRules mutationRules

infix fun JsonElement.withRules(mutationRules: List<IMutation>): JsonElement =
    mutationRules.fold(this) { q, w -> w.mutate(q) }

infix fun JsonStr.withParser(json: kotlinx.serialization.json.Json): JsonElement =
    json.parseToJsonElement(this.value)
