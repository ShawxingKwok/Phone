package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.resolver
import pers.shawxingkwok.phone.*
import pers.shawxingkwok.phone.apiNameInPhone
import pers.shawxingkwok.phone.getHeader
import pers.shawxingkwok.phone.getOrPost
import pers.shawxingkwok.phone.insertIf
import pers.shawxingkwok.phone.pathEnd

context (CodeFormatter)
internal fun KSFunctionDeclaration.getFileBody(
    ksclass: KSClassDeclaration,
    fileArgType: KSType,
    withToken: Boolean,
)
    : String
=
    """
    ${getHeader("Result<Pair<${fileArgType.text}, HttpResponse>>")} =
        ~runCatching{
            val response = client.${getOrPost(ksclass)}("${'$'}basicUrl/${ksclass.apiNameInPhone}/$pathEnd") {
                onEachRequest(this@${ksclass.apiNameInPhone}::class)
                extendRequest?.invoke(this)
                        
                ${insertIf(withToken){ "addToken()" }}

                ${getParametersBody(ksclass, ", ::parameter)")}
            }

            response.checkIsOK()

            ${
                if (fileArgType == resolver.builtIns.unitType)    
                    "Unit to response"
                else {
                    val serializerText = fileArgType.getSerializerText()
                    """
                    val headInfo = response.headers["Phone-Info"]
                        ?.let{ decode(response.bodyAsText(), $serializerText, ${getCipherTextForReturn(ksclass)}) }
                        ${insertIf(!fileArgType.isMarkedNullable){
                            "~?: error(\"Missed the head info of which the type is ${fileArgType.text}.\") !~"        
                        }}                        
                    
                    headInfo to response
                    """                    
                }
            }
        }!~
    """.trim()