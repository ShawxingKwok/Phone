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

internal inline fun mayEmbrace(
    condition: Boolean,
    getStart: () -> Unit,
    getEnd: () -> Unit,
    getBody: () -> Unit,
) {
    if (condition) getStart()
    getBody()
    if (condition) getEnd()
}