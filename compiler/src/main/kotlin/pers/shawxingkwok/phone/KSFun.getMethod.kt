package pers.shawxingkwok.phone

import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

internal fun KSFunctionDeclaration.getOrPost(ksclass: KSClassDeclaration): String =
    when {
        isAnnotationPresent(Phone.Get::class) -> "get"
        isAnnotationPresent(Phone.Post::class) -> "post"
        ksclass.isAnnotationPresent(Phone.Get::class) -> "get"
        ksclass.isAnnotationPresent(Phone.Post::class) -> "post"
        else -> Args.defaultGetOrPost
    }