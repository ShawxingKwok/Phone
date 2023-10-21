@file:Suppress("UnnecessaryVariable")

package pers.shawxingkwok.phone.validators

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclarationContainer
import com.google.devtools.ksp.symbol.Modifier
import pers.shawxingkwok.ksputil.*
import pers.shawxingkwok.phone.MyProcessor
import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.phone.getNeededFunctions

object PhoneValidator : KSDefaultValidator() {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): Boolean {
        val ksclass = classDeclaration

        when{
            MyProcessor.round > 0 -> {}

            ksclass.isAnnotationPresent(Phone.Api::class)
            && ksclass.isAnnotationPresent(Phone.WebSocket::class) ->
                Log.e(ksclass, "`Phone.Api` is needless when you set web sockets.")

            ksclass.isAnnotationPresent(Phone.Api::class)
            || ksclass.isAnnotationPresent(Phone.WebSocket::class) ->
            {
                Log.check(ksclass, ksclass.classKind == ClassKind.INTERFACE){
                    "The annotations `Phone.Api` and `Phone.WebSockets` could be annotated " +
                    "only on interfaces."
                }

                Log.check(ksclass, ksclass.packageName().any()){
                    "Each `Phone` interface should have a package name."
                }

                Log.check(ksclass, ksclass.getNeededFunctions().any()){
                    "Each `Phone` interface should contain at least one function. " +
                    "Note that functions in super classes also count."
                }

                ksclass.getNeededFunctions().forEach { ksfun ->
                    Log.check(
                        symbol = ksfun,
                        condition = ksfun.isAbstract
                            && Modifier.SUSPEND in ksfun.modifiers
                            && ksfun.typeParameters.none()
                            && ksfun.extensionReceiver == null
                    ) {
                        "In each `Phone` interface, all functions must be abstract, suspend, " +
                        "and without extensional receivers and type parameters."
                    }
                }

                val polymorphic = ksclass.getNeededFunctions()
                    .groupBy { it.simpleName() }
                    .values
                    .filter { it.size >= 2 }
                    .flatten()

                Log.check(
                    symbols = polymorphic,
                    condition = polymorphic.filterNot { it.isAnnotationPresent(Phone.Polymorphic::class) }.size <= 1
                ){
                    "Polymorphic functions in `Phone` interfaces should be annotated with `Phone.Polymorphic`. " +
                    "Note that if you make a common function polymorphic in later versions, the first common function " +
                    "shouldn't be annotated with `Phone.Polymorphic`, which means being backward compatible."
                }

                ksclass.getNeededFunctions().plus(ksclass).forEach {
                    Log.check(
                        symbol = it,
                        condition = !(it.isAnnotationPresent(Phone.Get::class)
                            && it.isAnnotationPresent(Phone.Post::class))
                    ){
                        "The annotations `Phone.Get` and `Phone.Post` can't be used together."
                    }
                }
            }

            ksclass.isAnnotationPresent(Phone.Api::class) -> {
                ksclass.getNeededFunctions().forEach { ksfun ->
                    Log.check(
                        ksfun,
                        !(ksfun.simpleName() == "handle"
                            && ksfun.typeParameters.none()
                            && ksfun.parameters.none()
                        )
                    ){
                        "There would be an additional function named `handle` for interception, " +
                        "Therefore, rename this function or add parameters."
                    }
                }
            }

            ksclass.isAnnotationPresent(Phone.WebSocket::class) -> {
                Log.check(
                    symbol = ksclass,
                    condition = ksclass.getNeededFunctions().all {
                        it.returnType!!.resolve() == resolver.builtIns.unitType
                    },
                ){
                    "Each function used by interfaces annotated with `Phone.WebSocket` " +
                        "can't have a return type.(Only `Unit` is allowed in other words.)"
                }
            }
        }

        return ksclass.getAllSuperTypes()
            .map { it.declaration }
            .plus(ksclass)
            .filterNot { it.containingFile == null }
            .all { it.accept(NestClassSkippedValidator, Unit) }
    }
}

private val verifiedDeclPaths = mutableSetOf<String>()

private object NestClassSkippedValidator : KSDefaultValidator() {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): Boolean {
        if (classDeclaration.qualifiedName()!! in verifiedDeclPaths)
            return true

        val isVerified = super.visitClassDeclaration(classDeclaration, data)

        if (isVerified) verifiedDeclPaths += classDeclaration.qualifiedName()!!

        return isVerified
    }

    override fun visitDeclarationContainer(declarationContainer: KSDeclarationContainer, data: Unit): Boolean =
        declarationContainer
            .declarations
            .filterNot { it is KSClassDeclaration }
            .allAccept()
}