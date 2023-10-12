package pers.shawxingkwok.phone

internal inline fun insertIf(
    condition: Boolean,
    getText: () -> String,
): String =
    if (condition) getText() else ""