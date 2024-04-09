package ru.vood.json.mutation.lib

import io.kotest.core.spec.style.ShouldSpec
import kotlinx.serialization.json.JsonObject
import ru.vood.json.mutation.lib.IMutation.Companion.add
import ru.vood.json.mutation.lib.Js.json

class ExampleDsl : ShouldSpec({

    should("Example Dsl for json in string and custom parser") {
        val jsonElement =
            JsonStr("{}") withParser json withRules listOf(
                "a1" add 1,
                "a2" add "asdas",
            )
        println(jsonElement)

    }

    should("Example Dsl for json in JsonObject") {
        val jsonElement =
            JsonObject(mapOf()) withRules listOf(
                "a1" add 1,
                "a2" add "asdas",
            )
        println(jsonElement)

    }

})