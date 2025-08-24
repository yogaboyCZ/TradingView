package cz.yogaboy.feature.stocks.domain

fun interface UseCase<I, O> { suspend operator fun invoke(input: I): O }
