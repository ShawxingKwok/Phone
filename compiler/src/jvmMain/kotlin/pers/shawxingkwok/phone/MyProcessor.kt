package pers.shawxingkwok.phone

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.symbol.*
import pers.shawxingkwok.ksputil.*
import pers.shawxingkwok.ktutil.allDo
import java.io.File

@Provide
internal object MyProcessor : KSProcessor{
    enum class Status{
        UNSTARTED, BUILT, COPIED
    }
    private var status = Status.UNSTARTED

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

                    var serializers = resolver
                        .getAnnotatedSymbols<Phone.Serializer, KSClassDeclaration>()
                        .associateBy { kclassDecl ->
                            kclassDecl.superTypes
                                .map{ it.resolve() }
                                .first { it.declaration.qualifiedName() == "kotlinx.serialization.KSerializer"  }
                                .arguments
                                .first()
                                .type!!
                                .resolve().also {
                                    check(!it.isMarkedNullable){
                                        TODO()
                                    }
                                }
                        }

                    @Suppress("SuspiciousCollectionReassignment")
                    serializers += serializers.mapKeys { (ksType, _) ->
                        if (ksType.isMarkedNullable) ksType.makeNotNullable()
                        else ksType.makeNullable()
                    }

                    val phones = allPaths.map { resolver.getClassDeclarationByName(it)!! }
                    buildClientConfig(phones, serializers)
                    buildServerConfig(phones, serializers)
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