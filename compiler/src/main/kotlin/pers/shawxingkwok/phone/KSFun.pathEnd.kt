package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.getAnnotationByType
import pers.shawxingkwok.ksputil.simpleName

internal val KSFunctionDeclaration.pathEnd: String get() =
    buildString {
        append(simpleName())

        val polymorphicAnnot = getAnnotationByType(Phone.Polymorphic::class)
            ?: return@buildString

        append("/${polymorphicAnnot.id}")
    }