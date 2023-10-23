package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.KSDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.getAnnotationByType

internal inline fun CodeFormatter.mayEmbraceWithAuth(
    decl: KSDeclaration,
    getBody: () -> String,
): String =
    buildString {
        val auth = decl.getAnnotationByType(Phone.Auth::class)

        if (auth != null) {
            append("${Types().authenticate}(\n")

            if (auth.configurations.any())
                auth.configurations.joinToString(
                    separator = ", ",
                    prefix = "configurations = arrayOf(",
                    postfix = "),\n"
                ){
                    if (it.any())
                        "\"$it\""
                    else
                        "null"
                }
                .let(::append)

            append("""
                    strategy = ${Types().AuthenticationStrategy}.${auth.strategy.name},
                ) {
            """.trimStart())
        }

        append(getBody())

        if (auth != null) append("\n}")
    }