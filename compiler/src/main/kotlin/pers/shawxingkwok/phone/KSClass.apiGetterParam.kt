package pers.shawxingkwok.phone

import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter

context (CodeFormatter)
internal val KSClassDeclaration.apiGetterParam: String
    get() = buildString {
        append("get$apiNameInPhone: (")

        if (!isAnnotationPresent(Phone.WebSocket::class))
            append(Types().PipelineContextUnitCall)

        append(") -> $apiNameInPhone")
    }