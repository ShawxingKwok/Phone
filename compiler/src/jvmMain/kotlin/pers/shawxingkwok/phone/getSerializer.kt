package pers.shawxingkwok.phone

import com.google.devtools.ksp.isOpen
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Variance
import pers.shawxingkwok.ksputil.resolver

internal fun KSValueParameter.getSerializer(): KSClassDeclaration? {
    val type = type.resolve()

    return if (isVararg)
        MyProcessor.serializers
        .entries
        .firstOrNull { (key, _) ->
            key.declaration == resolver.builtIns.arrayType.declaration
            && key.arguments.first().type!!.resolve() == type
            && (!type.declaration.isOpen() || key.arguments.first().variance == Variance.COVARIANT)
        }
        ?.value
    else
        MyProcessor.serializers[type]
}