package ru.vood.json.mutation.lib

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import org.junit.jupiter.api.Assertions
import ru.vood.json.mutation.lib.DeleteTest.Companion.json
import ru.vood.json.mutation.lib.DeleteTest.Companion.parseToJsonElement
import ru.vood.json.mutation.lib.IMutation.Companion.mutateToValue

data class TestCase(
    val description: String,
    val mutation: IMutation,
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
    val a4Value = A4("z", "x")
    val a4JsonElement = json.encodeToJsonElement(A4.serializer(), a4Value)
    withData(
        TestCase(
            "Добавление значения простого поля, логика в элемент массива",
            "a2/a3/a4[0]/f3" mutateToValue false,
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2","f3":false},{"f1":"f11","f2":"f22"}]}},"z1":15}""")
        ),
        TestCase(
            "Добавление значения простого поля, логика в не существующий элемент массива",
            "a2/a3/a4[2]/f3" mutateToValue false,
            Err("""json element a4 not contains index 2""")
        ),
        TestCase(
            "Добавление значения простого поля, логика",
            "z2" mutateToValue false,
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":15,"z2":false}""")
        ),
        TestCase(
            "Добавление значения простого поля, логика во вложенный объект",
            "z2/z3" mutateToValue false,
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":15,"z2":{"z3":false}}""")
        ),
        TestCase(
            "Добавление значения простого поля, число",
            "z2" mutateToValue 789,
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":15,"z2":789}""")
        ),
        TestCase(
            "Добавление значения простого поля, число во вложенный объект",
            "z2/z3" mutateToValue 789,
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":15,"z2":{"z3":789}}""")
        ),
        TestCase(
            "Добавление значения простого поля, строка",
            "z2" mutateToValue "789",
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":15,"z2":"789"}""")
        ),
        TestCase(
            "Добавление значения простого поля, строка во вложенный объект",
            "z2/z3" mutateToValue "789",
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":15,"z2":{"z3":"789"}}""")
        ),
        TestCase(
            "Добавление значения простого поля, с созданием объекта, логика",
            "z1/z2/z3" mutateToValue false,
            Err("""Unable add new object to JsonPrimitive""")
        ),
        TestCase(
            "Добавление значения поля, на целый объект",
            "a6" mutateToValue a4JsonElement,
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":15,"a6":{"f1":"z","f2":"x"}}""")
        ),
//        мутирование
        TestCase(
            "Мутирование значения простого поля, логика в элемент массива",
            "a2/a3/a4[0]/f2" mutateToValue false,
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":false},{"f1":"f11","f2":"f22"}]}},"z1":15}""")
        ),
        TestCase(
            "Мутирование значения поля, на целый объект",
            "a2" mutateToValue a4JsonElement,
            Ok("""{"a2":{"f1":"z","f2":"x"},"z1":15}""")
        ),
        TestCase(
            "Мутирование значения поля из не существующего массива",
            "a2[0]" mutateToValue a4JsonElement,
            Err("""json element a2 not JsonArray""")
        ),

        ) { (_, delete, expected) ->
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