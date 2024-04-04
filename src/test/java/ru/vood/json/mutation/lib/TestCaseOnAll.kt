package ru.vood.json.mutation.lib

data class TestCaseOnAll(
    val description: String,
    val jsonPath: JsonPath,
    val expectedAdd: IExpected,
    val expectedDelete: IExpected,
    val expectedMutate: IExpected,
)
