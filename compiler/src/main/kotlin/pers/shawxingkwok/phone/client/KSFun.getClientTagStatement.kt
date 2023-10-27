package pers.shawxingkwok.phone.client

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
    val tag: ${tagType.text} = response.headers["Phone-Tag"]
        ~?.let{ decode(it, $serializerText, ${getCipherTextForReturn(ksclass)}) }!~                        
        ${insertIf(!tagType.isMarkedNullable){
            "~?: error(\"Missed the head info of which the type is ${tagType.text}.\") !~"
        }}     
    """
}