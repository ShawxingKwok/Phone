package pers.shawxingkwok.phone

import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.KtGen
import pers.shawxingkwok.ksputil.Log
import pers.shawxingkwok.ksputil.getAnnotationByType

internal inline fun KtGen.mayEmbraceWithAuth(
    decl: KSDeclaration,
    getBody: () -> String,
): String =
    buildString {
        val auth = decl.getAnnotationByType<Phone.Auth>()

        val authenticate = getDeclText("io.ktor.server.auth.authenticate", null, true)
        val strategy = getDeclText("io.ktor.server.auth.AuthenticationStrategy", null, false)

        if (auth != null)
            append("""
                $authenticate(
                    configurations = arrayOf(${auth.configurations.joinToString(", "){ "\"$it\"" }}),
                    strategy = $strategy.${auth.strategy.name}
                ) {
            """.trimStart())

        append(getBody())

        if (auth != null) append("\n}")
    }