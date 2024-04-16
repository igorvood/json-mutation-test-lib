package ru.vood.json.mutation.lib

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import org.junit.jupiter.api.Assertions
import ru.vood.json.mutation.lib.DeleteTest.Companion.parseToJsonElement
import ru.vood.json.mutation.lib.IMutation.Companion.add
import ru.vood.json.mutation.lib.Js.json


class AddTest : FunSpec({
    val a4Value = A4("z", "x")
    val a4JsonElement = json.encodeToJsonElement(A4.serializer(), a4Value)
    withData(
        TestCase(
            "[1] Добавление значения простого поля, логика в элемент массива",
            "a2/a3/a4[0]/f3" add false,
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2","f3":false},{"f1":"f11","f2":"f22"}]}},"z1":15,"list":["P","O"]}""")
        ),
        TestCase(
            "[2] Добавление значения простого поля, логика в не существующий элемент массива",
            "a2/a3/a4[2]/f3" add false,
            Err("""json element a4 not contains index 2""")
        ),
        TestCase(
            "[3] Добавление значения простого поля, логика",
            "z2" add false,
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":15,"list":["P","O"],"z2":false}""")
        ),
        TestCase(
            "[4] Добавление значения простого поля, логика во вложенный объект",
            "z2/z3" add false,
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":15,"list":["P","O"],"z2":{"z3":false}}""")
        ),
        TestCase(
            "[5] Добавление значения простого поля, число",
            "z2" add 789,
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":15,"list":["P","O"],"z2":789}""")
        ),
        TestCase(
            "[6] Добавление значения простого поля, число во вложенный объект",
            "z2/z3" add 789,
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":15,"list":["P","O"],"z2":{"z3":789}}""")
        ),
        TestCase(
            "[7] Добавление значения простого поля, строка",
            "z2" add "789",
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":15,"list":["P","O"],"z2":"789"}""")
        ),
        TestCase(
            "[8] Добавление значения простого поля, строка во вложенный объект",
            "z2/z3" add "789",
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":15,"list":["P","O"],"z2":{"z3":"789"}}""")
        ),
        TestCase(
            "[9] Добавление значения простого поля, с созданием объекта, логика",
            "z1/z2/z3" add false,
            Err("""Add(jsonPath=JsonPath(value=z1/z2/z3), value=false) not compatible for JsonPrimitive with value 15""")
        ),
        TestCase(
            "[10] Добавление значения поля, на целый объект",
            "a6" add a4JsonElement,
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2"},{"f1":"f11","f2":"f22"}]}},"z1":15,"list":["P","O"],"a6":{"f1":"z","f2":"x"}}""")
        ),
//        мутирование
        TestCase(
            "[11] Добавление значения простого поля, логика в элемент массива",
            "a2/a3/a4[0]/f2" add false,
            Err("""In JsonObject found field 'a2/a3/a4[0]/f2' for Add(jsonPath=JsonPath(value=a2/a3/a4[0]/f2), value=false)""")
        ),
        TestCase(
            "[12] Добавление значения поля, на целый объект",
            "a2" add a4JsonElement,
            Err("""In JsonObject found field 'a2' for Add(jsonPath=JsonPath(value=a2), value={"f1":"z","f2":"x"})""")
        ),
        TestCase(
            "[13] Добавление значения поля из не существующего массива",
            "a2[0]" add a4JsonElement,
            Err("""Json element a2[0] not JsonArray, it has type JsonObject""")
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