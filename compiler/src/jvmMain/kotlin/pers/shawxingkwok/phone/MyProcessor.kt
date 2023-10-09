package pers.shawxingkwok.phone

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import pers.shawxingkwok.ksputil.*
import pers.shawxingkwok.ktutil.allDo
import java.io.File

@Provide
internal object MyProcessor : KSProcessor{
    enum class Status{
        UNSTARTED, BUILT, COPIED
    }
    private var status = Status.UNSTARTED

    val allSerializersProp = resolver
        .getAnnotatedSymbols<Phone.KSerializers, KSPropertyDeclaration>()
        .firstOrNull()

    private val allPaths = resolver
        .getAnnotatedSymbols<Phone, KSClassDeclaration>()
        .map { it.qualifiedName()!! }

    override fun process(round: Int): List<KSAnnotated> {
        val (valid, invalid) = resolver
            .getAnnotatedSymbols<Phone, KSClassDeclaration>()
            .partition { it.accept(KSDefaultValidator(), Unit) }

        // check
        valid.forEach { apiKSClass ->
            val functions = apiKSClass.getDeclaredFunctions()

            functions.forEach {
                // TODO("check")
            }

            check(apiKSClass.packageName().any()) {
                TODO()
            }
        }

        // also output to dest paths from ksp args
        when(status){
            Status.UNSTARTED ->
                if (invalid.none()) {
                    status = if (valid.any()) Status.BUILT else Status.COPIED
                    val all = allPaths.map { resolver.getClassDeclarationByName(it)!! }
                    buildClientConfig(all)
                    buildServerConfig(all)
                }

            Status.BUILT -> {
                status = Status.COPIED

                allDo(
                    listOf(Args.ServerPackagePath, Args.ServerPackageName, "Phone"),
                    listOf(Args.ClientPackagePath, Args.ClientPackageName, "Phone"),
                ){
                    (packagePath, packageName, fileName) ->

                    packagePath ?: return@allDo

                    val expectedEnd = packageName!!.replace(".", "/") + "/$fileName"

                    val file = Environment.codeGenerator
                        .previousGeneratedFiles
                        .first { it.path.endsWith(expectedEnd) }

                    // copy files if not in the test files round
                    if (file.path.endsWith("/test/resources/$expectedEnd"))
                        return emptyList()

                    val newFile = File("$packagePath/$expectedEnd.kt".replace("//", "/"))

                    if (!newFile.parentFile.exists())
                        newFile.parentFile.mkdirs()

                    if (!newFile.exists())
                        newFile.createNewFile()

                    newFile.writeBytes(file.readBytes())
                }
            }

            Status.COPIED -> {}
        }
        return invalid
    }
}