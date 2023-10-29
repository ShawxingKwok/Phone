package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.qualifiedName

context (CodeFormatter)
internal fun KSFunctionDeclaration.getMethodName(ksclass: KSClassDeclaration): String =
    if (kind is Kind.PartialContent)
        "Get"
    else
        getSelfMethodName() ?: ksclass.getSelfMethodName() ?: Args.defaultMethodName

context (CodeFormatter)
private fun KSDeclaration.getSelfMethodName(): String? =
    annotations.firstOrNull {
        it.annotationType.resolve().declaration.qualifiedName()!!.startsWith(Phone.Method::class.qualifiedName!!)
    }
    ?.shortName
    ?.asString()