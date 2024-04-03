package ru.vood.json.mutation.lib

import kotlinx.serialization.json.JsonElement
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.vood.json.mutation.lib.IMutation.Companion.delete
import ru.vood.json.mutation.lib.Js.json

internal class DeleteTest {


    @ParameterizedTest
    @MethodSource("ru.vood.json.mutation.lib.DeleteTest#testCaseData")
    fun test(testCase: TestCase) {
        println("Etalon json")
        println(parseToJsonElement.toString())

        when (val exp = testCase.expected) {
            is Ok -> {
                val mutate1 = testCase.mutation.mutate(parseToJsonElement)
                assertEquals(exp.expectedJson, mutate1.toString())
            }
            is Err -> {
                val textError = exp.expectedTextError
                kotlin.runCatching { testCase.mutation.mutate(parseToJsonElement) }
                    .map { error("must be exception") }
                    .getOrElse {
                        assertEquals(textError, it.message)
                    }
            }
        }
    }

    companion object {

        val a1 = A1(A2(A3(listOf(A4("f1", "f2"), A4("f11", "f22")))), 15)


        val parseToJsonElement: JsonElement = json.encodeToJsonElement(A1.serializer(), a1)

        private val testData = listOf(
            TestCase(
                "Удаление простого поля",
                delete { "z1" },
                Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":null}""")
            ),
            TestCase("Удаление целого объекта", delete { "a2/a3" }, Ok("""{"a2":{"a3":null},"z1":15}""")),
            TestCase("Удаление целого массива", delete { "a2/a3/a4" }, Ok("""{"a2":{"a3":{"a4":null}},"z1":15}""")),
            TestCase(
                "Удаление элемента массива",
                delete { "a2/a3/a4[0]" },
                Ok("""{"a2":{"a3":{"a4":[{"f1":"f11","f2":"f22"}]}},"z1":15}""")
            ),
            TestCase(
                "Удаление иного элемента массива",
                delete { "a2/a3/a4[1]" },
                Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"}]}},"z1":15}""")
            ),
            TestCase(
                "Удаление не существующего элемента массива",
                delete { "a2/a3/a4[3]" },
                Err("json element a4 not contains index 3")
            ),
            TestCase(
                "Удаление задано буквой элемента массива",
                delete { "a2/a3/a4[j]" },
                Err("For input string: \"j\"")
            ),
            TestCase(
                "Удаление не существующего элемента массива",
                delete { "a2/a3/a4[-1]" },
                Err("json element a4 not contains index -1")
            ),
            TestCase(
                "Удаление несуществующего объекта",
                delete { "a2/a33" },
                Err("In JsonObject not found field 'a33' for Delete(jsonPath=JsonPath(value=a2/a33))")
            ),
            TestCase(
                "Удаление поля в элементе массива",
                delete { "a2/a3/a4[1]/f1" },
                Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":null,"f2":"f22"}]}},"z1":15}""")
            ),
            TestCase(
                "Удаление не существующего поля",
                delete { "a21/a3/a4[1]/f1" },
                Err("""In JsonObject not found field 'a21' for Delete""")
            ),
        )


        @JvmStatic
        private fun testCaseData() = testData.map { Arguments.of(it) }

    }


}