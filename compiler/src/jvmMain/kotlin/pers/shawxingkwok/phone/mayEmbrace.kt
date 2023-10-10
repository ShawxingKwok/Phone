package pers.shawxingkwok.phone

internal inline fun mayEmbrace(
    condition: Boolean,
    start: String,
    end: String,
    getBody: () -> String,
) =
    buildString {
        if (condition) append(start)
        append(getBody())
        if (condition) append(end)
    }