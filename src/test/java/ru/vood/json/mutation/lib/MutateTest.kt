package ru.vood.json.mutation.lib

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import org.junit.jupiter.api.Assertions
import ru.vood.json.mutation.lib.DeleteTest.Companion.parseToJsonElement
import ru.vood.json.mutation.lib.IMutation.Companion.mutate1

data class TestCase(
    val description: String,
    val delete: IMutation,
    val expected: IExpected,
) : WithDataTestName {
    override fun dataTestName(): String = description
}

sealed interface IExpected

data class Ok(val expectedJson: String) : IExpected
data class Err(
    val expectedTextError: String,
    val throwable: Class<*> = IllegalStateException::class.java,
) : IExpected

class MutateTest : FunSpec({
    withData(
        TestCase(
            "изменение значения простого поля на строчку",
            "z1" mutate1 "q",
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":"q"}""")
        ),
        TestCase(
            "изменение значения простого поля на логику",
            "z1" mutate1 false,
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":false}""")
        ),
        TestCase(
            "изменение значения простого поля на число",
            "z1" mutate1 256,
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":256}""")
        ),
        TestCase(
            "Добавление значения простого поля, число",
            "z2" mutate1 256,
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":15,"z2":256}""")
        ),
        TestCase(
            "Добавление значения простого поля, строка",
            "z2" mutate1 "256",
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":15,"z2":"256"}""")
        ),

        TestCase(
            "Добавление значения простого поля, логика",
            "z2" mutate1 false,
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":15,"z2":false}""")
        ),
        TestCase(
            "Добавление значения простого поля, с созданием объекта, логика",
            "z1/z2/z3" mutate1 false,
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":15,"z2":false}""")
        ),

        ) { (a, delete, expected) ->
        println("Etalon json")
        println(parseToJsonElement.toString())

        when (expected) {
            is Ok -> {
                val mutate1 = delete.mutate(parseToJsonElement)
                Assertions.assertEquals(expected.expectedJson, mutate1.toString())
            }
            is Err -> {
                val textError = expected.expectedTextError
                kotlin.runCatching { delete.mutate(parseToJsonElement) }
                    .map { error("must be exception") }
                    .getOrElse {
                        Assertions.assertEquals(textError, it.message)
                    }
            }
        }

    }
})