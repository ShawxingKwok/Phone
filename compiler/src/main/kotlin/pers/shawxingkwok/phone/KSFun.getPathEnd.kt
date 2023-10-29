package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.simpleName

internal fun KSFunctionDeclaration.getPathEnd(ksclass: KSClassDeclaration): String =
    buildString {
        append(simpleName())
        val polymorphicId = getCall(ksclass).polymorphicId ?: return@buildString
        append("/$polymorphicId")
    }