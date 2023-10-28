package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.getAnnotationByType

data class MethodInfo(val name: String, val withForm: Boolean)

internal fun KSFunctionDeclaration.getMethodInfo(ksclass: KSClassDeclaration): MethodInfo =
    when(kind){
        is Kind.PartialContent -> "Get" to false

        is Kind.Common, is Kind.Manual ->
            getSelfMethodInfo()
            ?: ksclass.getSelfMethodInfo()
            ?: (Args.defaultMethodName to (Args.defaultMethodName != "Get"))

        is Kind.WebSocket -> getSelfMethodInfo() ?: ("Get" to false)
    }
    .let { MethodInfo(it.first, it.second) }

private fun KSDeclaration.getSelfMethodInfo(): Pair<String, Boolean>? {
    getAnnotationByType(Phone.Method.Get::class)?.let { return "Get" to false }
    getAnnotationByType(Phone.Method.Post::class)?.let { return "Post" to it.withForm }
    getAnnotationByType(Phone.Method.Put::class)?.let { return "Put" to it.withForm }
    getAnnotationByType(Phone.Method.Delete::class)?.let { return "Delete" to it.withForm }
    getAnnotationByType(Phone.Method.Patch::class)?.let { return "Patch" to it.withForm }
    return null
}