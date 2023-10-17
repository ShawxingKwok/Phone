package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.ksputil.noPackageName
import pers.shawxingkwok.ksputil.qualifiedName

private val cache = mutableMapOf<String, String>()

internal val KSClassDeclaration.phoneName: String get() =
    cache.getOrPut(qualifiedName()!!) {
        noPackageName()!!.replace(".", "_")
    }