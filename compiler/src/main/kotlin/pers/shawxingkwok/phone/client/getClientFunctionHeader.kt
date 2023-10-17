package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.KtGen
import pers.shawxingkwok.ksputil.simpleName
import pers.shawxingkwok.phone.insertIf

context (KtGen)
internal fun KSFunctionDeclaration.getClientFunctionHeader(): String =
    buildString {
        append("override suspend fun ${simpleName()}(")

        if (parameters.size <= 2)
            parameters.joinToString(postfix = ")", separator = ", ") {
                "${insertIf(it.isVararg) { "vararg " }}${it.name!!.asString()}: ${it.type.text}"
            }
            .let(::append)
        else
            parameters.joinToString(prefix = "\n", postfix = ",\n)", separator = ",\n") {
                "${insertIf(it.isVararg) { "vararg " }}${it.name!!.asString()}: ${it.type.text}"
            }
            .let(::append)
    }