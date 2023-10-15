package pers.shawxingkwok.phone

import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.KtGen
import pers.shawxingkwok.ksputil.Log
import pers.shawxingkwok.ksputil.getAnnotationByType

internal fun KtGen.mayEmbraceWithAuth(
    ksclass: KSClassDeclaration,
    ksfun: KSFunctionDeclaration?,
    getBody: () -> String,
): String =
    buildString {
        Log.require(
            condition = !ksclass.isAnnotationPresent(Phone.Auth::class)
                || ksfun == null
                || !ksfun.isAnnotationPresent(Phone.Auth::class),
            symbols = listOfNotNull(ksclass, ksfun)
        ){
            "You can't annotate with `Phone.Auth` on both interface and functions."
        }

        val auth = ksclass.getAnnotationByType<Phone.Auth>() ?: ksfun?.getAnnotationByType<Phone.Auth>()

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