package pers.shawxingkwok.phone

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.symbol.*
import pers.shawxingkwok.ksputil.*
import pers.shawxingkwok.ktutil.allDo
import java.io.File

@Suppress("unused")
@Provide
internal object MyProcessor : KSProcessor{
    private object Status{
        const val UNSTARTED = 0
        const val BUILT = 1
        const val COPIED = 2

        var value = UNSTARTED
    }

    private val phonePaths = resolver
        .getAnnotatedSymbols<Phone, KSClassDeclaration>()
        .map { it.qualifiedName()!! }

    lateinit var serializers: Map<KSType, KSClassDeclaration>
        private set

    override fun process(round: Int): List<KSAnnotated> {
        if (phonePaths.none())
            return emptyList()

        val (valid, invalid) = resolver
            .getAnnotatedSymbols<Phone, KSClassDeclaration>()
            .partition { it.accept(KSDefaultValidator(), Unit) }

        // check each Phone class
        valid.forEach { ksclass ->
            Log.require(ksclass.classKind == ClassKind.INTERFACE, ksclass){
                "The annotation `Phone` could be annotated only on interfaces."
            }
            Log.require(ksclass.typeParameters.none(), ksclass){
                "Interfaces annotated with `Phone` can't have any type parameter."
            }
            Log.require(ksclass.parentDeclaration == null, ksclass){
                "Each interface annotated with `Phone` can't be a nest class. " +
                "Or the simple generated declaration names and routes may repeat."
            }
            Log.require(ksclass.packageName().any(), ksclass){
                "Class without package name is commonly used in test cases. " +
                "However, I don't want to spend time adapting `Phone` with it."
            }
        }

        // check all functions in Phone classes
        valid.flatMap { it.getAllFunctions() }
            .filterNot {
                val name = it.simpleName()
                name == "toString"
                || name == "equals"
                || name == "hashCode"
            }
            .forEach {
                Log.require(
                    condition =
                        !it.isAbstract
                        || Modifier.SUSPEND in it.modifiers && it.typeParameters.none(),
                    symbol = it,
                ){
                    "In each class annotated with `Phone`, " +
                    "all abstract functions must be suspend and without type parameters, except 'toString', 'equals', and 'hashCode'."
                }
            }

        // also output to dest paths from ksp args
        when(Status.value){
            Status.UNSTARTED -> {
                if (invalid.any()) return invalid

                Status.value++

                serializers = resolver
                    .getAnnotatedSymbols<Phone.Serializer, KSClassDeclaration>()
                    .associateBy { ksclass ->
                        Log.require(ksclass.classKind == ClassKind.OBJECT, ksclass) {
                            "Each class annotated with `Phone.Serializer` must be object."
                        }

                        ksclass.superTypes
                            .map { it.resolve() }
                            .firstOrNull { it.declaration.qualifiedName() == "kotlinx.serialization.KSerializer" }
                            .let {
                                Log.require(
                                    condition = it != null,
                                    symbol = ksclass,
                                ) {
                                    "Each class annotated with `Phone.Serializer` must implement `KSerializer`."
                                }
                                it
                            }
                            .arguments
                            .first()
                            .type!!
                            .resolve()
                            .also {
                                if (it.isMarkedNullable)
                                    Log.w(
                                        "The nullable symbol is needless in the inner type `$it` embraced by `KSerializer`.",
                                        ksclass
                                    )
                            }
                    }

                serializers += serializers.mapKeys { (ksType, _) ->
                    if (ksType.isMarkedNullable) ksType.makeNotNullable()
                    else ksType.makeNullable()
                }

                val phones = phonePaths.map { resolver.getClassDeclarationByName(it)!! }
                buildClientPhone(phones)
                buildServerPhone(phones)
            }

            Status.BUILT -> {
                Status.value++

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