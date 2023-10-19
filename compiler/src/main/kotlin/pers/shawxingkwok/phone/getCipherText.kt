package pers.shawxingkwok.phone

import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import pers.shawxingkwok.ksputil.CodeFormatter

context (CodeFormatter)
internal fun KSFunctionDeclaration.getCipherTextForReturn(srcKSClass: KSClassDeclaration): String? {
    val isCrypto =
        returnType!!.isAnnotationPresent(Phone.Crypto::class)
        || isAnnotationPresent(Phone.Crypto::class)
        // parent function may be from a super class
        || srcKSClass.isAnnotationPresent(Phone.Crypto::class)

    return getCipherText(isCrypto)
}

context (CodeFormatter)
internal fun KSValueParameter.getCipherText(srcKSClass: KSClassDeclaration): String? {
    val isCrypto =
        isAnnotationPresent(Phone.Crypto::class)
        || this.type.isAnnotationPresent(Phone.Crypto::class)
        || (parent as KSFunctionDeclaration).isAnnotationPresent(Phone.Crypto::class)
        || srcKSClass.isAnnotationPresent(Phone.Crypto::class)

    return getCipherText(isCrypto)
}

context (CodeFormatter)
private fun getCipherText(isCrypto: Boolean): String? =
    if (isCrypto)
        MyProcessor.cipherKSObj?.text ?: error(TODO("link"))
    else
        null