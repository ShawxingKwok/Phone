package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Variance
import pers.shawxingkwok.ksputil.*

private val cache = mutableMapOf<KSClassDeclaration, Boolean>().alsoRegister()

private val KSClassDeclaration.isAnnotatedWithSerializable: Boolean get() =
    cache.getOrPut(this){
        annotations.any { ksAnnot ->
            ksAnnot.shortName.getShortName() == "Serializable"
            && ksAnnot.annotationType.resolve().declaration.qualifiedName() == "kotlinx.serialization.Serializable"
        }
    }

context (CodeFormatter)
internal fun KSValueParameter.getSerializerText(): String? {
    val type = type.resolve()

    return if (isVararg)
        MyProcessor.serializers.entries
            .firstOrNull { (key, _) ->
                key.declaration == resolver.builtIns.arrayType.declaration
                && key.arguments.first().type!!.resolve() == type
                && key.arguments.first().variance == Variance.COVARIANT
            }
            ?.value
            ?.text
    else
        type.getSerializerText()
}

context (CodeFormatter)
internal fun KSType.getSerializerText(): String? =
    when(val serializer = MyProcessor.serializers[this]){
        null -> (declaration as KSClassDeclaration)
                .takeIf { it.isAnnotatedWithSerializable }
                ?.let { "${it.text}.serializer()" }

        else -> serializer.text
    }