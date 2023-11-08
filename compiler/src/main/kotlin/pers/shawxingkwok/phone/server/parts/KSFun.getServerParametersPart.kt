package pers.shawxingkwok.phone.server.parts

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.resolver
import pers.shawxingkwok.phone.getCipherText
import pers.shawxingkwok.phone.getSerializerText

context (CodeFormatter)
internal fun KSFunctionDeclaration.getServerParametersPart(
    ksclass: KSClassDeclaration,
    start: String,
    onError: (String) -> String = { """respondBadRequest("$it")""" }
) =
    parameters.joinToString("", prefix = "\n") { param ->
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
        $paramName = try{
                ~val text = params["$paramName"] ?: return@$start ${onError("Not found `${paramName}` in received parameters.")}
                decode<$paramTypeText>(text, ${param.getSerializerText()}, ${param.getCipherText(ksclass)})
            }catch(tr: Throwable){
                ${onError("The parameter `${paramName}` is incorrectly serialized to \${params[\"${paramName}\"]}.")}
                return@$start
            },!~
            
        """.trimStart()
    }