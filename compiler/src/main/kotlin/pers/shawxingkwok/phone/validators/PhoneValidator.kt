@file:Suppress("UnnecessaryVariable")

package pers.shawxingkwok.phone.validators

import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.*
import pers.shawxingkwok.ksputil.*
import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.phone.getCall
import pers.shawxingkwok.phone.getNeededFunctions

object PhoneValidator : KSDefaultValidator() {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): Boolean {
        val ksclass = classDeclaration

        val isValid = !classDeclaration.asStarProjectedType().isError
            && classDeclaration.superTypes.allAccept()
            && visitDeclaration(classDeclaration, data)
            && visitDeclarationContainer(classDeclaration, data)
            && classDeclaration.superTypes.all {
                val decl = it.resolve().declaration
                decl.containingFile == null ||  decl.accept(this, data)
            }

        if (ksclass.isAnnotationPresent(Phone.Api::class)) run{
            Log.check(ksclass, ksclass.classKind == ClassKind.INTERFACE) {
                "The annotations `Phone.Api` and could be annotated " +
                "only on interfaces."
            }

            Log.check(ksclass, ksclass.packageName().any()) {
                "Each `Phone` interface should have a package name."
            }

            if (!isValid) return@run

            ksclass.getNeededFunctions().forEach { ksfun ->
                Log.check(
                    symbol = ksfun,
                    condition = ksfun.isAbstract
                        && Modifier.SUSPEND in ksfun.modifiers
                        && ksfun.typeParameters.none()
                        && ksfun.returnType!!.resolve() == resolver.builtIns.anyType
                        && ksfun.extensionReceiver == null
                        && ksfun.annotations.filter {
                                it.annotationType.resolve().declaration.qualifiedName()!!
                                .startsWith(Phone.Call::class.qualifiedName!!)
                            }.count() == 1
                ) {
                    "In each `Phone` interface, all functions, including those from super classes but except overridden, " +
                    "must be abstract, suspend, return `Any`, without extensional receivers and type parameters, " +
                    "and carry an annotation in `Phone.Call`."
                }
            }

            val polymorphic = ksclass.getNeededFunctions()
                .groupBy { it.simpleName() }
                .values
                .filter { it.size >= 2 }
                .flatten()

            Log.check(
                symbols = polymorphic,
                condition = polymorphic.filter { it.getCall(ksclass).polymorphicId == null }.size <= 1
            ){
                "Polymorphic functions in `Phone` interfaces should be annotated with `Phone.Polymorphic`. " +
                "Note that if you make a common function polymorphic in later versions, the first common function " +
                "shouldn't be annotated with `Phone.Polymorphic`, which means being backward compatible."
            }
        }

        return isValid
    }

    @Suppress("NonAsciiCharacters")
    private val `Unit？` = resolver.builtIns.unitType.makeNullable()

    override fun visitTypeReference(typeReference: KSTypeReference, data: Unit): Boolean {
        val isValid =  super.visitTypeReference(typeReference, data)

        if (isValid && typeReference.resolve() == `Unit？`)
            Log.e(typeReference, "Use `Boolean` instead of `Unit?`.")

        return isValid
    }
}