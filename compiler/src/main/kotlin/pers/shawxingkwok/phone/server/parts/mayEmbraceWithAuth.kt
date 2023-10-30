package pers.shawxingkwok.phone.server.parts

import com.google.devtools.ksp.symbol.KSDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.getAnnotationByType
import pers.shawxingkwok.phone.Decls
import pers.shawxingkwok.phone.Phone

context (CodeFormatter)
internal inline fun KSDeclaration.mayEmbraceWithAuth(getBody: () -> String): String =
    buildString {
        val auth = getAnnotationByType(Phone.Auth::class)

        if (auth != null) {
            append("${Decls().authenticate}(\n")

            if (auth.configurations.any())
                auth.configurations.joinToString(
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