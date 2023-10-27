package pers.shawxingkwok.phone

private object InFutureError : Error() {
    private fun readResolve(): Any = InFutureError
}

fun InFuture() : Nothing{
    throw InFutureError
}