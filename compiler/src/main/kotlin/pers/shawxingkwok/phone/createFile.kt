package pers.shawxingkwok.phone

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.Delicate
import pers.shawxingkwok.ksputil.Environment
import pers.shawxingkwok.ksputil.createFile

@OptIn(Delicate::class)
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
                    .plus(phones.flatMap { it.getAllSuperTypes().map(KSType::declaration) })
                    .plus(MyProcessor.cipherKSObj)
                    .plus(MyProcessor.serializers.values)
                    .mapNotNull { it?.containingFile }
                    .toTypedArray(),
            ),
        fileName = "Phone",
        header = """
            |@file:Suppress(
            |    "LocalVariableName", "SameParameterValue", "unused", "PropertyName", "HttpUrlsUsage",
            |    "ClassName", "KotlinRedundantDiagnosticSuppress", "DuplicatedCode", 
            |    "MemberVisibilityCanBePrivate", "TestFunctionName"            
            |)
            """.trimMargin(),
        extensionName = "",
        initialImports = initialImports,
        getBody = getBody,
    )
}