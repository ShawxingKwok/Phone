package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.getAnnotationByType

internal val KSFunctionDeclaration.mayPolymorphicId: String get() {
    val polymorphicAnnot = getAnnotationByType(Phone.Polymorphic::class) ?: return ""
    return "/${polymorphicAnnot.id}"
}