package ru.vood.json.mutation.lib

import kotlinx.serialization.json.JsonElement
import ru.vood.json.mutation.lib.Js.json

infix fun JsonStr.withRule(mutationRules: List<IMutation>): JsonElement =
    json.parseToJsonElement(this.value) withRule mutationRules

infix fun JsonElement.withRule(mutationRules: List<IMutation>): JsonElement =
    mutationRules.fold(this) { q, w -> w.mutate(q) }

infix fun JsonStr.withParser(json: kotlinx.serialization.json.Json): JsonElement =
    json.parseToJsonElement(this.value)
