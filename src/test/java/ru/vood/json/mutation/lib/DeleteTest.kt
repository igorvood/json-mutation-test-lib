package ru.vood.json.mutation.lib

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.vood.json.mutation.lib.IMutation.Companion.delete
import javax.security.auth.callback.ConfirmationCallback.OK

internal class DeleteTest {


    @ParameterizedTest
    @MethodSource("ru.vood.json.mutation.lib.DeleteTest#testCaseData")

    fun mutate(testCase: TestCase) {
        println("Etalon json")
        println(parseToJsonElement.toString())

        when(val exp = testCase.expected){
            is Ok -> {
                val mutate1 = testCase.delete.mutate(parseToJsonElement)
                assertEquals(exp.expectedJson, mutate1.toString())
            }
            is Err -> {
                val textError = exp.expectedTextError
                kotlin.runCatching { testCase.delete.mutate(parseToJsonElement) }
                    .map { error("must be exception") }
                    .getOrElse {
                        assertEquals(textError, it.message)
                    }
            }
        }
    }

    companion object {
        val json = Json { }


        val a1 = A1(A2(A3(listOf(A4("f1", "f2"), A4("f11", "f22")))), 15)


        val parseToJsonElement: JsonElement = json.encodeToJsonElement(A1.serializer(), a1)

        private val testData = listOf(
            TestCase("Удаление простого поля", "z1".delete(), Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":null}""")),
            TestCase("Удаление целого объекта", "a2/a3".delete(), Ok("""{"a2":{"a3":null},"z1":15}""")),
            TestCase("Удаление целого массива", "a2/a3/a4".delete(), Ok("""{"a2":{"a3":{"a4":null}},"z1":15}""")),
            TestCase("Удаление элемента массива", "a2/a3/a4[0]".delete(), Ok("""{"a2":{"a3":{"a4":[{"f1":"f11","f2":"f22"}]}},"z1":15}""")),
            TestCase("Удаление иного элемента массива", "a2/a3/a4[1]".delete(), Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"}]}},"z1":15}""")),
            TestCase("Удаление не существующего элемента массива", "a2/a3/a4[3]".delete(), Err("json element a4 not contains index 3")),
            TestCase("Удаление задано буквой элемента массива", "a2/a3/a4[j]".delete(), Err("For input string: \"j\"")),
            TestCase("Удаление не существующего элемента массива", "a2/a3/a4[-1]".delete(), Err("json element a4 not contains index -1")),
            TestCase("Удаление несуществующего объекта", "a2/a33".delete(), Err("In JsonObject not found field 'a33' for delete")),
            TestCase("Удаление поля в элементе массива", "a2/a3/a4[1]/f1".delete(), Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":null,"f2":"f22"}]}},"z1":15}""")),
        )


        @JvmStatic
        private fun testCaseData() = testData.map { Arguments.of(it) }

    }

    data class TestCase(
        val description: String,
        val delete: Delete,
        val expected: IExpected
    )

    sealed interface IExpected

    data class Ok(val expectedJson: String) : IExpected
    data class Err(
        val expectedTextError: String,
        val throwable: Class<*> = IllegalStateException::class.java
    ) : IExpected
}