package ru.vood.json.mutation.lib

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.vood.json.mutation.lib.IMutation.Companion.delete

internal class DeleteTest {


    @ParameterizedTest
    @MethodSource("ru.vood.json.mutation.lib.DeleteTest#testCaseData")

    fun mutate(testCase: TestCase) {



        if (testCase.expected!=null) {
            val mutate1 = testCase.delete.mutate(parseToJsonElement)
            assertEquals(testCase.expected, mutate1.toString())
        } else{
            val textError = testCase.textError!!

            val assertThrows =
                assertThrows(IllegalStateException::class.java, { testCase.delete.mutate(parseToJsonElement) })

            assertEquals(textError,  assertThrows.message)
        }

    }

    companion object {
        val json = Json { }

        val parseToJsonElement = json.parseToJsonElement("""{"a1":{"a2":{"a3":{"a4_2":null}}}}""")

        private val path = "a1/a2/a3"

        private val testData = listOf(
            TestCase("a1/a2/a3".delete(), """{"a1":{"a2":{"a3":null}}}""", "json element a33 not found for delete"),
            TestCase("a1/a2/a33".delete(), null, "json element a33 not found for delete"),
//            TestCase(path mutate "false", "\"false\""),
//            TestCase(path mutate 15, "15"),
//            TestCase(path.mutate(), "null"),
        )


        @JvmStatic
        private fun testCaseData() = testData.map { Arguments.of(it) }

    }

    data class TestCase(val delete: Delete, val expected: String?, val textError: String)
}