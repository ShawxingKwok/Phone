package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.ksputil.noPackageName
import pers.shawxingkwok.ksputil.qualifiedName

private val cache = mutableMapOf<String, String>()

internal val KSClassDeclaration.apiNameInPhone: String get() =
    cache.getOrPut(qualifiedName()!!) {
        noPackageName()!!.replace(".", "_")
    }

internal val KSClassDeclaration.apiPropNameInPhone: String get() =
    apiNameInPhone.replaceFirstChar { it.lowercase() }