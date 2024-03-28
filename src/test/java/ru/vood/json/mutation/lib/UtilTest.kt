package ru.vood.json.mutation.lib

import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.vood.json.mutation.lib.Util.mutate

internal class UtilTest {

    @Test
    fun mutate() {
        val path = "a1/a2/a3"
        val jsonElement = path mutate false

        val expected = """{"a1":{"a2":{"a3":false}}}"""
        assertEquals(expected, jsonElement.toString())

        val path2 = path + "/a4"
        val jsonObject = path2 mutate "asd"

        assertEquals("""{"a1":{"a2":{"a3":{"a4":"asd"}}}}""", jsonObject.toString())

        val jsonObject1 = path + "/a4_1" mutate 123

        assertEquals("""{"a1":{"a2":{"a3":{"a4_1":123}}}}""", jsonObject1.toString())

        val mutate = (path + "/a4_2").mutate()

        assertEquals("""{"a1":{"a2":{"a3":{"a4_2":null}}}}""", mutate.toString())


        val plus = JsonObject(mutate.plus(jsonObject1).plus(jsonObject).plus(jsonElement))

        val listOf = setOf(mutate, jsonObject1, jsonObject, jsonElement)

    }

    @ParameterizedTest
    @MethodSource("ru.vood.json.mutation.lib.UtilTest#testCaseData")
    fun asda(jsonObject: TestCase) {
        assertEquals("""{"a1":{"a2":{"a3":${jsonObject.expected}}}}""", jsonObject.jsonObject.toString())
    }

    companion object {
        private val path = "a1/a2/a3"

        private val testData = listOf(
            TestCase(path mutate false, "false"),
            TestCase(path mutate "false", "\"false\""),
            TestCase(path mutate 15, "15"),
            TestCase(path.mutate(), "null"),
        )


        @JvmStatic
        private fun testCaseData() = testData.map { Arguments.of(it) }

    }

    data class TestCase(val jsonObject: JsonObject, val expected: String)

}