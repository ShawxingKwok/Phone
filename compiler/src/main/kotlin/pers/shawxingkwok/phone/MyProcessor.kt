package pers.shawxingkwok.phone

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.*
import pers.shawxingkwok.ksputil.*
import pers.shawxingkwok.ktutil.allDo
import pers.shawxingkwok.ktutil.fastLazy
import pers.shawxingkwok.phone.client.buildClientPhone
import pers.shawxingkwok.phone.validators.PhoneValidator
import pers.shawxingkwok.phone.validators.SerializerValidator
import java.io.File

@Suppress("unused")
@Provide
internal object MyProcessor : KSProcessor{
    private object Status{
        const val UNSTARTED = 0
        const val BUILT = 1

        var value = UNSTARTED
    }

    private val phoneInterfacePaths = resolver
        .getAnnotatedSymbols<Phone.Api, KSClassDeclaration>()
        .plus(resolver.getAnnotatedSymbols<Phone.WebSocket, KSClassDeclaration>())
        .also { ksclasses ->
            val cognominal= ksclasses
                .groupBy { it.apiNameInPhone }
                .values
                .filter { it.size >= 2 }
                .flatten()

            Log.check(cognominal, cognominal.none()){
                "`Phone` interfaces can't share names."
            }
        }
        .map { it.qualifiedName()!! }

    private val serializerPaths = resolver
        .getAnnotatedSymbols<Phone.Serializer, KSClassDeclaration>()
        .map { it.qualifiedName()!! }

    val phones by fastLazy {
        check(Status.value != Status.UNSTARTED)
        phoneInterfacePaths.map { resolver.getClassDeclarationByName(it)!! }
    }

    val hasWebSocket by fastLazy {
        check(Status.value != Status.UNSTARTED)
        phones.any { ksclass ->
            ksclass.getNeededFunctions().any {
                it.isAnnotationPresent(Phone.WebSocket::class)
            }
        }
    }

    val anyNotWebSocket by fastLazy {
        check(Status.value != Status.UNSTARTED)
        phones.any { ksclass ->
            ksclass.getNeededFunctions().any {
                !it.isAnnotationPresent(Phone.WebSocket::class)
            }
        }
    }

    // both nullable and non-nullable are mapped
    val serializers: Map<KSType, KSClassDeclaration> by fastLazy {
        check(Status.value != Status.UNSTARTED)

        @Suppress("LocalVariableName")
        val _serializers = serializerPaths
            .map { resolver.getClassDeclarationByName(it)!! }
            .associateBy { ksclass ->
                ksclass.superTypes
                    .map { it.resolve() }
                    .first { it.declaration.qualifiedName() == "kotlinx.serialization.KSerializer" }
                    .arguments
                    .first()
                    .type!!
                    .resolve()
            }

        _serializers + _serializers.mapKeys { (ksType, _) ->
            if (ksType.isMarkedNullable) ksType.makeNotNullable()
            else ksType.makeNullable()
        }
    }

    val cipherKSObj: KSClassDeclaration? by fastLazy {
        val ksclasses = resolver.getAnnotatedSymbols<Phone.Crypto, KSClassDeclaration>()
            .filterNot { it.classKind == ClassKind.INTERFACE }

        Log.check(
            symbols = ksclasses,
            condition = ksclasses.size <= 1
                && (ksclasses.none() || ksclasses.first().classKind == ClassKind.OBJECT)
        ){
            "`Phone.Crypto` could be annotated only on interfaces and a single object."
        }

        val ksclass = ksclasses.firstOrNull() ?: return@fastLazy null

        val superCipherType = resolver
            .getClassDeclarationByName(Phone.Cipher::class.qualifiedName!!)!!
            .asStarProjectedType()

        Log.check(
            symbol = ksclass,
            condition = superCipherType.isAssignableFrom(ksclass.asStarProjectedType())
        ){
            "The object annotated with `@Phone.Crypto` should be a subclass of `Phone.Cipher`."
        }

        ksclass
    }

    var round = 0
        private set

    override fun process(round: Int): List<KSAnnotated> {
        this.round = round

        if (phoneInterfacePaths.none())
            return emptyList()

        val invalid = resolver
            .getAnnotatedSymbols<Phone.Api, KSClassDeclaration>()
            .plus(resolver.getAnnotatedSymbols<Phone.WebSocket, KSClassDeclaration>())
            .filterNot { it.accept(PhoneValidator, Unit) }
            .toMutableList()

        invalid += resolver.getAnnotatedSymbols<Phone.Serializer, KSClassDeclaration>()
            .filterNot { it.accept(SerializerValidator, Unit) }

        // also output to dest paths from ksp args
        when(Status.value){
            Status.UNSTARTED -> {
                if (invalid.any())
                    return invalid

                Status.value++

                buildClientPhone()
                buildServerPhone()
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
        }
        return invalid
    }
}