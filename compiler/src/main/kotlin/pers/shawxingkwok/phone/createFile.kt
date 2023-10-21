package pers.shawxingkwok.phone

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.Environment
import pers.shawxingkwok.ksputil.createFile

internal fun createFile(
    phones: List<KSClassDeclaration>,
    packageName: String,
    initialImports: Set<String>,
    getBody: CodeFormatter.() -> String,
) {
    Environment.codeGenerator.createFile(
        packageName = packageName,
        dependencies =
            Dependencies(
                aggregating = true,
                sources = phones
                    .plus(MyProcessor.cipherKSObj)
                    .plus(MyProcessor.serializers.values)
                    .mapNotNull { it?.containingFile }
                    .toTypedArray(),
            ),
        fileName = "Phone",
        header = """
            @file:Suppress("LocalVariableName", "SameParameterValue", "unused", "PropertyName", "HttpUrlsUsage")
            """.trim(),
        extensionName = "",
        initialImports = initialImports,
        getBody = getBody,
    )
}