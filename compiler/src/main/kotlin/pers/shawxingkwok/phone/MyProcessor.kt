package pers.shawxingkwok.phone

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.*
import pers.shawxingkwok.ksputil.*
import pers.shawxingkwok.ktutil.allDo
import pers.shawxingkwok.phone.client.buildClientPhone
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

    private val phoneInterfacePaths = resolver
        .getAnnotatedSymbols<Phone.Api, KSClassDeclaration>()
        .plus(resolver.getAnnotatedSymbols<Phone.WebSocket, KSClassDeclaration>())
        .also { ksclasses ->
            val cognominal= ksclasses
                .groupBy { it.phoneName }
                .values
                .filter { it.size >= 2 }
                .flatten()

            Log.require(cognominal, cognominal.none()){
                "`Phone` interfaces can't share names."
            }
        }
        .map { it.qualifiedName()!! }

    // both nullable and non-nullable are mapped
    lateinit var serializers: Map<KSType, KSClassDeclaration>
        private set

    var cipherKSObj: KSClassDeclaration? = null
        private set

    override fun process(round: Int): List<KSAnnotated> {
        if (phoneInterfacePaths.none())
            return emptyList()

        var (valid, invalid) = resolver
            .getAnnotatedSymbols<Phone.Api, KSClassDeclaration>()
            .plus(resolver.getAnnotatedSymbols<Phone.WebSocket, KSClassDeclaration>())
            .partition { ksclass ->
                ksclass.getAllSuperTypes()
                    .map { it.declaration }
                    .filter { it.containingFile != null }
                    .all {
                        // there are few super interfaces, so there is no need to
                        // make caches for `accept` to accelerate.
                        it.accept(KSDefaultValidator(), Unit)
                    }
            }

        // check each class with Phone.Api
        valid.forEach { ksclass ->
            Log.require(
                symbol = ksclass,
                condition = !ksclass.isAnnotationPresent(Phone.Api::class)
                    || !ksclass.isAnnotationPresent(Phone.WebSocket::class)
            ){
                "`Phone.Api` is needless when you set web sockets."
            }
            Log.require(ksclass, ksclass.classKind == ClassKind.INTERFACE){
                "The annotations `Phone.Api` and `Phone.WebSockets` could be annotated " +
                "only on interfaces."
            }
            Log.require(ksclass, ksclass.packageName().any()){
                "Each interface with `Phone` should have a package name."
            }
            if (ksclass.isAnnotationPresent(Phone.WebSocket::class)){
                Log.require(ksclass, ksclass.getNeededFunctions().any()){
                    "Each interface annotated with `Phone.WebSocket` should contain at least one function. " +
                    "Note that functions in super classes also count."
                }
                Log.require(
                    symbol = ksclass,
                    condition = ksclass.getNeededFunctions().all {
                        it.returnType!!.resolve() == resolver.builtIns.unitType
                    },
                ){
                    "Each function used by interfaces annotated with `Phone.WebSocket` " +
                    "can't have a return type.(Only `Unit` is allowed in other words.)"
                }
            }
            val polymorphic = ksclass.getNeededFunctions()
                .groupBy { it.simpleName() }
                .values
                .filter { it.size >= 2 }
                .flatten()

            Log.require(
                symbols = polymorphic,
                condition = polymorphic.filterNot { it.isAnnotationPresent(Phone.Polymorphic::class) }.size <= 1
            ){
                "Polymorphic functions in interfaces with `Phone` should be annotated with `Phone.Polymorphic`. " +
                "Note that if you make a common function polymorphic in later versions, the first common function " +
                "shouldn't be annotated with `Phone.Polymorphic`, which means being backward compatible."
            }
        }

        // check all functions in Phone classes
        valid.flatMap { it.getNeededFunctions() }
            .forEach { ksfun ->
                Log.require(
                    symbol = ksfun,
                    condition =
                        ksfun.isAbstract
                        && Modifier.SUSPEND in ksfun.modifiers
                        && ksfun.typeParameters.none()
                        && ksfun.extensionReceiver == null
                ) {
                    "In each class with `Phone`, all functions must be abstract, suspend, " +
                    "and without extensional receivers and type parameters, except 'toString', " +
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

                val phones = phoneInterfacePaths.map { resolver.getClassDeclarationByName(it)!! }
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