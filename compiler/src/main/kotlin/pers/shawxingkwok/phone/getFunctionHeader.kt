package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.simpleName

context (CodeFormatter)
internal fun KSFunctionDeclaration.getHeader(returned: String): String =
    buildString {
        append("override ")

        if(Modifier.SUSPEND in modifiers)
            append("suspend ")

        append("fun ${simpleName()}(")

        if (parameters.size <= 2)
            append("${parameters.joinToString(", "){ it.text }}): $returned")
        else
            append("""
                    ${ parameters.joinToString(separator = "\n") { "${it.text}," }}
                )
                    : $returned 
            """)
    }

context (CodeFormatter)
private val KSValueParameter.text get() =
    buildString {
        if (isVararg) append("vararg ")
        append("${name!!.asString()}: ${type.text}")
    }