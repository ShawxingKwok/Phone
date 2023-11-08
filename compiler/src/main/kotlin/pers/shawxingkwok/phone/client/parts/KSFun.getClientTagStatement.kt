package pers.shawxingkwok.phone.client.parts

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.phone.getCipherTextForReturn
import pers.shawxingkwok.phone.getSerializerText
import pers.shawxingkwok.phone.insertIf

context (CodeFormatter)
internal fun KSFunctionDeclaration.getClientTagStatement(ksclass: KSClassDeclaration, tagType: KSType): String{
    val serializerText = tagType.getSerializerText()

    return """
    val text = response.headers["Phone-Tag"] ?: error("Not found the header `Phone-Tag`.")
    val tag = decode<${tagType.text}>(text, $serializerText, ${getCipherTextForReturn(ksclass)}) 
    """
}