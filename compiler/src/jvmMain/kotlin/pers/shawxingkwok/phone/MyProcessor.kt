package pers.shawxingkwok.phone

import com.google.devtools.ksp.getAllSuperTypes
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

    private val phoneApiPaths = resolver
        .getAnnotatedSymbols<Phone.Api, KSClassDeclaration>()
        .map { it.qualifiedName()!! }

    // both nullable and non-nullable are mapped
    lateinit var serializers: Map<KSType, KSClassDeclaration>
        private set

    var cipherKSObj: KSClassDeclaration? = null
        private set

    override fun process(round: Int): List<KSAnnotated> {
        if (phoneApiPaths.none())
            return emptyList()

        var (valid, invalid) = resolver
            .getAnnotatedSymbols<Phone.Api, KSClassDeclaration>()
            .partition { it.accept(KSDefaultValidator(), Unit) }

        // check each class with Phone.Api
        valid.forEach { ksclass ->
            Log.require(ksclass.classKind == ClassKind.INTERFACE, ksclass){
                "The annotation `Phone.Api` could be annotated only on interfaces."
            }
            Log.require(ksclass.typeParameters.none(), ksclass){
                "Interfaces annotated with `Phone.Api` can't have any type parameter."
            }
            Log.require(ksclass.parentDeclaration == null, ksclass){
                "Each interface annotated with `Phone.Api` can't be a nest class. " +
                "Or the simple generated declaration names and routes may repeat."
            }
            Log.require(ksclass.packageName().any(), ksclass){
                "Non-local class without package name is commonly used in test cases. " +
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
                        || Modifier.SUSPEND in it.modifiers && it.typeParameters.none()
                        && it.extensionReceiver == null,
                    symbol = it,
                ){
                    "In each class annotated with `Phone.Api`, " +
                    "all abstract functions must be suspend without extensional receivers and " +
                    "type parameters, except 'toString', 'equals', and 'hashCode'."
                }
            }

        val cipherKSObj = resolver
            .getAnnotatedSymbols<Phone.Crypto, KSClassDeclaration>()
            .filter { it.classKind == ClassKind.OBJECT }
            .also {
                if (round > 0) return@also

                Log.require(it.size <= 1, it){
                    "Multiple crypto objects are forbidden."
                }
            }
            .firstOrNull()

        if (cipherKSObj?.accept(KSDefaultValidator(), Unit) == false)
            invalid += cipherKSObj

        // also output to dest paths from ksp args
        when(Status.value){
            Status.UNSTARTED -> {
                if (invalid.any())
                    return invalid

                Status.value++

                this.cipherKSObj = cipherKSObj

                if (cipherKSObj != null)
                    Log.require(
                        cipherKSObj.getAllSuperTypes().any {
                            it.declaration.qualifiedName() == Phone.Cipher::class.qualifiedName
                        },
                        cipherKSObj
                    ){
                        "The object annotated with `@Phone.Crypto` should implement `Phone.Cipher`."
                    }

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
                                    "Each class annotated with `Phone.Serializer` must declare `KSerializer` in its implementations."
                                }
                                it
                            }
                            .arguments
                            .first()
                            .type!!
                            .resolve()
                    }

                serializers += serializers.mapKeys { (ksType, _) ->
                    if (ksType.isMarkedNullable) ksType.makeNotNullable()
                    else ksType.makeNullable()
                }

                val phones = phoneApiPaths.map { resolver.getClassDeclarationByName(it)!! }
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