package pers.shawxingkwok.phone.validators

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.ksputil.KSDefaultValidator
import pers.shawxingkwok.ksputil.Log
import pers.shawxingkwok.ksputil.qualifiedName
import pers.shawxingkwok.phone.MyProcessor

object SerializerValidator : KSDefaultValidator() {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): Boolean {
        val kserializerType = classDeclaration.superTypes.firstOrNull {
            it.resolve().declaration.qualifiedName() == "kotlinx.serialization.KSerializer"
        }

        if (MyProcessor.round == 0) {
            Log.check(classDeclaration, classDeclaration.classKind == ClassKind.OBJECT) {
                "Each class annotated with `Phone.Serializer` must be object."
            }

            Log.check(
                symbol = classDeclaration,
                condition = kserializerType != null
            ) {
                "Custom serializers should declare `KSerializer` in the basic super types."
            }
        }

        return kserializerType!!.accept(this, Unit)
    }
}