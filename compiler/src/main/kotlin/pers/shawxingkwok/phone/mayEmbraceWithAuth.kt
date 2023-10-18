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

        val authenticate = getDeclText("io.ktor.server.auth.authenticate", null, true)
        val strategy = getDeclText("io.ktor.server.auth.AuthenticationStrategy", null, false)

        if (auth != null)
            append("""
                $authenticate(
                    configurations = arrayOf(${auth.configurations.joinToString(", "){ "\"$it\"" }}),
                    strategy = $strategy.${auth.strategy.name},
                ) {
            """)

        append(getBody())

        if (auth != null) append("\n}")
    }