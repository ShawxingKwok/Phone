package pers.shawxingkwok.phone

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import pers.shawxingkwok.ksputil.KtGen

context (KtGen)
@OptIn(KspExperimental::class)
internal fun KSFunctionDeclaration.getCipherTextForReturn(): String? {
    val isCrypto =
        returnType!!.isAnnotationPresent(Phone.Crypto::class)
        || isAnnotationPresent(Phone.Crypto::class)
        || parentDeclaration!!.isAnnotationPresent(Phone.Crypto::class)

    return MyProcessor.cipherKSObj?.takeIf { isCrypto }?.text
}

context (KtGen)
@OptIn(KspExperimental::class)
internal fun KSValueParameter.getCipherText(): String? {
    val ksFun = parent as KSFunctionDeclaration

    val isCrypto =
        isAnnotationPresent(Phone.Crypto::class)
        || this.type.isAnnotationPresent(Phone.Crypto::class)
        || ksFun.isAnnotationPresent(Phone.Crypto::class)
        || ksFun.parentDeclaration!!.isAnnotationPresent(Phone.Crypto::class)

    return MyProcessor.cipherKSObj?.takeIf { isCrypto }?.text
}