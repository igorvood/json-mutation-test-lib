package ru.vood.json.mutation.lib

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import org.junit.jupiter.api.Assertions
import ru.vood.json.mutation.lib.IMutation.Companion.add
import ru.vood.json.mutation.lib.IMutation.Companion.delete

data class CompositeTestCase(
    val description: String,
    val mutations: List<IMutation>,
    val expected: IExpected,
) : WithDataTestName {
    override fun dataTestName(): String = description
}


class CompositeTest : FunSpec({
    withData(
        listOf(
            CompositeTestCase(
                description = "добавление и удаление",
                mutations = listOf(
                    delete { "a2" },
                    "a1/a3/a4/f3" add false,
                ),
                expected = Ok("""{"a2":null,"z1":15,"list":["P","O"],"a1":{"a3":{"a4":{"f3":false}}}}""")
            ),
            CompositeTestCase(description = "удаление и добавление",
                mutations = listOf(
                    "a2/a3/a4[0]/f3" add false,
                    delete { "a2/a3/a4[0]/f3" }
                ),
                expected = Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2","f3":null},{"f1":"f11","f2":"f22"}]}},"z1":15,"list":["P","O"]}""")),
        )
    ) { (_, mutationList, expected) ->
        println("Etalon json")
        println(DeleteTest.parseToJsonElement.toString())

        when (expected) {
            is Ok -> {
                val mutate1 = JsonStr(DeleteTest.parseToJsonElement.toString()) withRules mutationList
                Assertions.assertEquals(expected.expectedJson, mutate1.toString())
            }

            is Err -> {
                val textError = expected.expectedTextError
                kotlin.runCatching {
                    JsonStr(DeleteTest.parseToJsonElement.toString()) withRules mutationList
                }
                    .map { error("must be exception") }
                    .getOrElse {
                        Assertions.assertEquals(textError, it.message)
                    }
            }
        }
    }
})