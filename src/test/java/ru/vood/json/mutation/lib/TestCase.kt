package ru.vood.json.mutation.lib

import io.kotest.datatest.WithDataTestName

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