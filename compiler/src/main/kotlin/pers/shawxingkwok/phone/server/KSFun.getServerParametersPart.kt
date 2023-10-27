package pers.shawxingkwok.phone.server

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.resolver
import pers.shawxingkwok.phone.getCipherText
import pers.shawxingkwok.phone.getSerializerText
import pers.shawxingkwok.phone.insertIf

context (CodeFormatter)
internal fun KSFunctionDeclaration.getServerParametersPart(
    ksclass: KSClassDeclaration,
    start: String,
    onError: (String) -> String = { text ->
        """
        call.respondText(
            text = "$text",
            status = HttpStatusCode.BadRequest
        )
        """.trim()
    }
) = buildString{
    if (parameters.any()) append("\n")

    parameters.forEach { param ->
        val paramName = param.name!!.asString()
        val type = param.type.resolve()

        val paramTypeText =
            if (param.isVararg) when (type) {
                resolver.builtIns.booleanType -> BooleanArray::class.text
                resolver.builtIns.charType -> CharArray::class.text

                resolver.builtIns.byteType -> ByteArray::class.text
                resolver.builtIns.shortType -> ShortArray::class.text
                resolver.builtIns.intType -> IntArray::class.text
                resolver.builtIns.longType -> LongArray::class.text

                resolver.builtIns.floatType -> FloatArray::class.text
                resolver.builtIns.doubleType -> DoubleArray::class.text

                else -> Array::class.text + "<out ${type.text}>"
            }
            else
                type.text

        """
        $paramName = params["$paramName"]
            ~?.let{
                try{
                    decode<${paramTypeText.removeSuffix("?")}>(it, ${param.getSerializerText()}, ${param.getCipherText(ksclass)})
                }catch(tr: Throwable){
                    ${onError("The parameter `${paramName}` is incorrectly serialized.\\n${'$'}tr")}
                    return@$start
                }
            }!~
            ${insertIf(!type.isMarkedNullable) {
                "~?: return@$start ${onError("Not found `${paramName}` in received parameters.")}!~"
            }} 
        """.trim()
        .let(::append)

        insert(length - 2, ",")
        append("\n\n")
    }
}