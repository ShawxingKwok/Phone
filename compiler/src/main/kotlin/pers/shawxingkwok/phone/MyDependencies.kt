package pers.shawxingkwok.phone

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration

fun MyDependencies(phones: List<KSClassDeclaration>) : Dependencies =
    Dependencies(
        aggregating = true,
        sources = phones
            .plus(MyProcessor.cipherKSObj)
            .plus(MyProcessor.serializers.values)
            .mapNotNull { it?.containingFile }
            .toTypedArray(),
    )