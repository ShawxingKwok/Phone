package pers.shawxingkwok.phone

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isAnnotationPresent
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
            .plus(resolver.getAnnotatedSymbols<Phone.WebSockets, KSClassDeclaration>())
            .partition { it.accept(KSDefaultValidator(), Unit) }

        // check each class with Phone.Api
        valid.forEach { ksclass ->
            Log.require(
                symbol = ksclass,
                condition = !ksclass.isAnnotationPresent(Phone.Api::class)
                    || !ksclass.isAnnotationPresent(Phone.WebSockets::class)
            ){
                "`Phone.Api` is needless when you set web sockets."
            }
            Log.require(ksclass, ksclass.classKind == ClassKind.INTERFACE){
                "The annotations `Phone.Api` and `Phone.WebSockets` could be annotated " +
                "only on interfaces."
            }
            Log.require(ksclass, ksclass.parentDeclaration == null){
                "Each interface with `Phone` can't be a nest class. Or the simple " +
                "generated declaration names and routes may repeat."
            }
            Log.require(ksclass, ksclass.packageName().any()){
                "Each interface with `Phone` should have a package name."
            }

            val polymorphic = ksclass.getAllFunctions()
                .groupBy { it.simpleName() }
                .values
                .filter { it.size >= 2 }
                .flatten()

            Log.require(polymorphic, polymorphic.none()){
                "Polymorphic functions are forbidden in interfaces with `Phone`, " +
                "because it's error-prone when ensuring backward compatibility."
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
            .filter { it.isAbstract }
            .forEach { ksfun ->
                Log.require(
                    symbol = ksfun,
                    condition = Modifier.SUSPEND in ksfun.modifiers
                        && ksfun.typeParameters.none()
                        && ksfun.extensionReceiver == null,
                ) {
                    "In each class with `Phone`, all abstract functions must be suspend " +
                    "without extensional receivers and type parameters, except 'toString', " +
                    "'equals', and 'hashCode'."
                }
            }

        val cipherKSObj = resolver
            .getAnnotatedSymbols<Phone.Crypto, KSClassDeclaration>()
            .filter { it.classKind == ClassKind.OBJECT }
            .also {
                if (round > 0) return@also

                Log.require(it, it.size <= 1){
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
                        cipherKSObj,
                        cipherKSObj.getAllSuperTypes().any {
                            it.declaration.qualifiedName() == Phone.Cipher::class.qualifiedName
                        },
                    ){
                        "The object annotated with `@Phone.Crypto` should implement `Phone.Cipher`."
                    }

                serializers = resolver
                    .getAnnotatedSymbols<Phone.Serializer, KSClassDeclaration>()
                    .associateBy { ksclass ->
                        Log.require(ksclass, ksclass.classKind == ClassKind.OBJECT) {
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