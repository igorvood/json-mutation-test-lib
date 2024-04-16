package ru.vood.json.mutation.lib

import io.kotest.core.spec.style.ShouldSpec
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Assertions
import ru.vood.json.mutation.lib.IMutation.Companion.add
import ru.vood.json.mutation.lib.IMutation.Companion.mutateTo
import ru.vood.json.mutation.lib.Js.json

class ExampleDsl : ShouldSpec({

    should("Example Dsl for json in string and custom parser") {
        val jsonElement =
            JsonStr("{}") withParser json withRules listOf(
                "a1" add 1,
                "a2" add "asdas",
            )
        Assertions.assertEquals("""{"a1":1,"a2":"asdas"}""", jsonElement.toString())
    }

    should("Example Dsl for json in JsonObject") {
        val jsonElement =
            JsonObject(mapOf()) withRules listOf(
                "a1" add 1,
                "a2" add "asdas",
            )
        Assertions.assertEquals("""{"a1":1,"a2":"asdas"}""", jsonElement.toString())
    }

    should("Example Dsl for json in JsonObject by String") {
        val jsonElement =
            JsonObject(mapOf()) withRules listOf(
                "a1" add JsonStr("""{"j":true}"""),
                "a2" add "asdas",
                "a2" mutateTo JsonStr("""{"k":13}"""),
                "a3" add JsonObject(mapOf("k" to JsonPrimitive(15))),
            )
        Assertions.assertEquals("""{"a1":{"j":true},"a2":{"k":13},"a3":{"k":15}}""", jsonElement.toString())
    }
})