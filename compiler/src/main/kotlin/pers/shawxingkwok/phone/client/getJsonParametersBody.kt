package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.phone.getCipherText
import pers.shawxingkwok.phone.getSerializerText

context (CodeFormatter)
internal fun KSFunctionDeclaration.getJsonParametersBody(ksclass: KSClassDeclaration): String =
    parameters.joinToString("\n") { ksParam ->
        listOf(
            "\"${ksParam.name!!.asString()}\"",
            ksParam.name!!.asString(),
            ksParam.getSerializerText(),
            ksParam.getCipherText(ksclass),
        )
        .joinToString(separator = ", ", prefix = "jsonParameter(", postfix = ")")
    }