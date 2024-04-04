package ru.vood.json.mutation.lib

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions
import ru.vood.json.mutation.lib.IMutation.Companion.add
import ru.vood.json.mutation.lib.IMutation.Companion.delete
import ru.vood.json.mutation.lib.IMutation.Companion.mutateTo

class TestAll : FunSpec({
//    Etalon json: {"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":15}
    withData(
        listOf(
            TestCaseOnAll(
                description = "значения простого поля для существующего сложного json объекта",
                jsonPath = JsonPath("""a2"""),
                expectedAdd = Err("In JsonObject found field 'a2' for Add(jsonPath=JsonPath(value=a2), value=1.0)"),
                expectedMutate = Ok("""{"a2":1.0,"z1":15,"list":["P","O"]}"""),
                expectedDelete = Ok("""{"a2":null,"z1":15,"list":["P","O"]}"""),
            ),
            TestCaseOnAll(
                description = "значения простого поля для существующего сложного json объекта в массиве",
                jsonPath = JsonPath("""a2/a3/a4[0]"""),
                expectedAdd = Err("Allowed number [2] for JsonArray a2/a3/a4[0] but it equals [0] for Add(jsonPath=JsonPath(value=a2/a3/a4[0]), value=1.0)"),
                expectedMutate = Ok("""{"a2":{"a3":{"a4":[{"f1":"f11","f2":"f22"},1.0]}},"z1":15,"list":["P","O"]}"""),
                expectedDelete = Ok("""{"a2":{"a3":{"a4":[{"f1":"f11","f2":"f22"}]}},"z1":15,"list":["P","O"]}"""),
            ),
            TestCaseOnAll(
                description = "значения простого поля для не существующего сложного json объекта в массиве",
                jsonPath = JsonPath("""a2/a3/a4[2]"""),
                expectedAdd = Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"},1.0]}},"z1":15,"list":["P","O"]}"""),
                expectedMutate = Err("""Allowed range [0, 1] for JsonArray a2/a3/a4[2] but it not contains index 2 for Mutate(jsonPath=JsonPath(value=a2/a3/a4[2]), value=1.0)"""),
                expectedDelete = Err("""Allowed range [0, 1] for JsonArray a2/a3/a4[2] but it not contains index 2 for Delete(jsonPath=JsonPath(value=a2/a3/a4[2]))"""),
            ),
            TestCaseOnAll(
                description = "значения простого поля для существующего простого поля json объекта",
                jsonPath = JsonPath("""z1"""),
                expectedAdd = Err("In JsonObject found field 'z1' for Add(jsonPath=JsonPath(value=z1), value=1.0)"),
                expectedMutate = Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":1.0,"list":["P","O"]}"""),
                expectedDelete = Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":null,"list":["P","O"]}"""),
            ),
            TestCaseOnAll(
                description = "значения простого поля для не существующего простого поля json объекта",
                jsonPath = JsonPath("""z99"""),
                expectedAdd = Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":15,"list":["P","O"],"z99":1.0}"""),
                expectedMutate = Err("""In JsonObject not found field 'z99' for Mutate(jsonPath=JsonPath(value=z99), value=1.0)"""),
                expectedDelete = Err("""In JsonObject not found field 'z99' for Delete(jsonPath=JsonPath(value=z99))"""),
            ),
        ).flatMap { ta ->
            listOf(
                TestCase(
                    "${Delete::class.simpleName} ${ta.description}",
                    delete { ta.jsonPath.value },
                    ta.expectedDelete
                ),
                TestCase(
                    "${Mutate::class.simpleName} ${ta.description}, с числом",
                    ta.jsonPath.value mutateTo 1.0,
                    ta.expectedMutate
                ),
                TestCase(
                    "${Add::class.simpleName} ${ta.description}, с числом",
                    ta.jsonPath.value add 1.0,
                    ta.expectedAdd
                ),

                )
        }
    ) { (_, delete, expected) ->
        println("Etalon json")
        println(DeleteTest.parseToJsonElement.toString())

        when (expected) {
            is Ok -> {
                val mutate1 = delete.mutate(DeleteTest.parseToJsonElement)
                mutate1.toString() shouldBe  expected.expectedJson
            }
            is Err -> {
                val textError = expected.expectedTextError
                kotlin.runCatching { delete.mutate(DeleteTest.parseToJsonElement) }
                    .map { error("must be exception") }
                    .getOrElse {
                        Assertions.assertEquals(textError, it.message)
                    }
            }
        }

    }
}
)