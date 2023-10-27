package pers.shawxingkwok.phone.server

import com.google.devtools.ksp.symbol.KSDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.getAnnotationByType
import pers.shawxingkwok.phone.Decls
import pers.shawxingkwok.phone.Phone

internal inline fun CodeFormatter.mayEmbraceWithAuth(
    decl: KSDeclaration,
    getBody: () -> String,
): String =
    buildString {
        val auth = decl.getAnnotationByType(Phone.Feature.Auth::class)

        if (auth != null) {
            append("${Decls().authenticate}(\n")

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
                    strategy = ${Decls().AuthenticationStrategy}.${auth.strategy.name},
                ) {
            """.trimStart())
        }

        append(getBody())

        if (auth != null) append("\n}")
    }