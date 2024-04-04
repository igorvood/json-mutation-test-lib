package ru.vood.json.mutation.lib

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import org.junit.jupiter.api.Assertions
import ru.vood.json.mutation.lib.DeleteTest.Companion.parseToJsonElement
import ru.vood.json.mutation.lib.IMutation.Companion.mutateTo
import ru.vood.json.mutation.lib.Js.json

class MutateTest : FunSpec({
    val a4Value = A4("z", "x")
    val a4JsonElement = json.encodeToJsonElement(A4.serializer(), a4Value)
    withData(
        TestCase(
            "[1] Мутирование значения простого поля, логика в элемент массива",
            "a2/a3/a4[0]/f3" mutateTo false,
            Err("""In JsonObject not found field 'a2/a3/a4[0]/f3' for Mutate(jsonPath=JsonPath(value=a2/a3/a4[0]/f3), value=false)""")
        ),
        TestCase(
            "[2] Добавление значения простого поля, логика в не существующий элемент массива",
            "a2/a3/a4[2]/f3" mutateTo false,
            Err("""json element a4 not contains index 2""")
        ),
        TestCase(
            "[3] Добавление значения простого поля, логика",
            "z2" mutateTo false,
            Err("""In JsonObject not found field 'z2' for Mutate(jsonPath=JsonPath(value=z2), value=false)""")
        ),
        TestCase(
            "[4] Добавление значения простого поля, логика во вложенный объект",
            "z2/z3" mutateTo false,
            Err("""In JsonObject not found field 'z2' for Mutate(jsonPath=JsonPath(value=z2/z3), value=false)""")
        ),
        TestCase(
            "[5] Добавление значения простого поля, число",
            "z2" mutateTo 789,
            Err("""In JsonObject not found field 'z2' for Mutate(jsonPath=JsonPath(value=z2), value=789)""")
        ),
        TestCase(
            "[6] Добавление значения простого поля, число во вложенный объект",
            "z2/z3" mutateTo 789,
            Err("""In JsonObject not found field 'z2' for Mutate(jsonPath=JsonPath(value=z2/z3), value=789)""")
        ),
        TestCase(
            "[7] Добавление значения простого поля, строка",
            "z2" mutateTo "789",
            Err("""In JsonObject not found field 'z2' for Mutate(jsonPath=JsonPath(value=z2), value="789")""")
        ),
        TestCase(
            "[8] Добавление значения простого поля, строка во вложенный объект",
            "z2/z3" mutateTo "789",
            Err("""In JsonObject not found field 'z2' for Mutate(jsonPath=JsonPath(value=z2/z3), value="789")""")
        ),
        TestCase(
            "[9] Добавление значения простого поля, с созданием объекта, логика",
            "z1/z2/z3" mutateTo false,
            Err("""Mutate(jsonPath=JsonPath(value=z1/z2/z3), value=false) not compatible for JsonPrimitive with value 15""")
        ),
        TestCase(
            "[10] Добавление значения поля, на целый объект",
            "a6" mutateTo a4JsonElement,
            Err("""In JsonObject not found field 'a6' for Mutate(jsonPath=JsonPath(value=a6), value={"f1":"z","f2":"x"})""")
        ),
//        мутирование
        TestCase(
            "[11] Мутирование значения простого поля, логика в элемент массива",
            "a2/a3/a4[0]/f2" mutateTo false,
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":false},{"f1":"f11","f2":"f22"}]}},"z1":15}""")
        ),
        TestCase(
            "[13] Мутирование значения поля, на целый объект",
            "a2" mutateTo a4JsonElement,
            Ok("""{"a2":{"f1":"z","f2":"x"},"z1":15}""")
        ),
        TestCase(
            "[15] Мутирование значения поля из не существующего массива",
            "a2[0]" mutateTo a4JsonElement,
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