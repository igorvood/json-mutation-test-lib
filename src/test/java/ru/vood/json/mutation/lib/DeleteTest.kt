package ru.vood.json.mutation.lib

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.vood.json.mutation.lib.IMutation.Companion.delete

internal class DeleteTest {


    @ParameterizedTest
    @MethodSource("ru.vood.json.mutation.lib.DeleteTest#testCaseData")

    fun mutate(testCase: TestCase) {

        println(parseToJsonElement.toString())

        if (testCase.expected!=null) {
            val mutate1 = testCase.delete.mutate(parseToJsonElement)
            assertEquals(testCase.expected, mutate1.toString())
        } else{
            val textError = testCase.textError!!

            val assertThrows =
                assertThrows(IllegalStateException::class.java) { testCase.delete.mutate(parseToJsonElement) }

            assertEquals(textError,  assertThrows.message)
        }

    }

    companion object {
        val json = Json { }



        val a1 = A1(A2(A3(listOf(A4("f1", "f2"),A4("f11", "f22")))))


        val parseToJsonElement: JsonElement = json.encodeToJsonElement(A1.serializer(), a1)

        private val path = "a1/a2/a3"

        private val testData = listOf(
            TestCase("a2/a3".delete(), """{"a2":{"a3":null}}""", null),
            TestCase("a2/a3/a4".delete(), """{"a2":{"a3":{"a4":null}}}""", null),
            TestCase("a2/a3/a4[0]".delete(), "asdasd", "asdsad"),
            TestCase("a2/a33".delete(), null, "json element a33 not found for delete"),
        )


        @JvmStatic
        private fun testCaseData() = testData.map { Arguments.of(it) }

    }

    data class TestCase(val delete: Delete, val expected: String?, val textError: String?)
}