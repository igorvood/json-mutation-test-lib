package ru.vood.json.mutation.lib

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import org.junit.jupiter.api.Assertions
import ru.vood.json.mutation.lib.IMutation.Companion.delete
import ru.vood.json.mutation.lib.IMutation.Companion.mutateToValue

data class CompositeTestCase(
    val description: String,
    val mutation: List<IMutation>,
    val expected: IExpected,
) : WithDataTestName {
    override fun dataTestName(): String = description
}



class CompositeTest: FunSpec({
    withData(
        CompositeTestCase("добавление и удаление",
            listOf(
                delete { "a2" },
                "a1/a3/a4/f3" mutateToValue false,
            ),
            Ok("""{"a2":null,"z1":15,"a1":{"a3":{"a4":{"f3":false}}}}""")
        ),
        CompositeTestCase("удаление и добавление",
            listOf(
                "a2/a3/a4[0]/f3" mutateToValue false,
                delete { "a2/a3/a4[0]/f3" }
            ),
            Ok("""{"a2":{"a3":{"a4":[{"f1":"f1","f2":"f2","f3":null},{"f1":"f11","f2":"f22"}]}},"z1":15}""")
        ),
    ){(q,mutationList,expected) ->
        println("Etalon json")
        println(DeleteTest.parseToJsonElement.toString())

        when (expected) {
            is Ok -> {
                val mutate1 = mutationList.fold(DeleteTest.parseToJsonElement) { q, w -> w.mutate(q) }
                Assertions.assertEquals(expected.expectedJson, mutate1.toString())
            }
            is Err -> {
                val textError = expected.expectedTextError
                kotlin.runCatching { mutationList.fold(DeleteTest.parseToJsonElement) { q, w -> w.mutate(q) } }
                    .map { error("must be exception") }
                    .getOrElse {
                        Assertions.assertEquals(textError, it.message)
                    }
            }
        }
    }
})