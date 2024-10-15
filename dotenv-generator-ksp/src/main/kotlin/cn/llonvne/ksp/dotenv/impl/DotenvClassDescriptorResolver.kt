package cn.llonvne.ksp.dotenv.impl

import cn.llonvne.Dotenv
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver

class DotenvClassDescriptorResolver(
    private val environment: SymbolProcessorEnvironment
) {

    data class DotenvClassDescriptor(
        val classDeclaration: KSClassDeclaration,
        val dotenv: Dotenv,
        val properties: List<DotenvFieldDescriptor>
    ) {
        private val simpleName: String by lazy {
            classDeclaration.simpleName.asString()
        }

        val className: ClassName by lazy {
            classDeclaration.toClassName()
        }

        val loaderFunctionName: String by lazy {
            dotenv.loaderFunctionName.replace("%T", simpleName)
        }

        fun resolvePrefix(): String {
            return if (dotenv.prefix == "") {
                ""
            } else {
                dotenv.prefix + dotenv.prefixNameSpilt
            }
        }
    }

    data class DotenvFieldDescriptor(
        val property: KSPropertyDeclaration,
        val filed: Dotenv.Field
    ) {

        fun resolveFieldType(): KSType {
            return property.type.resolve()
        }

        fun resolveKeyName(dotenvClassDescriptor: DotenvClassDescriptor): String {
            return dotenvClassDescriptor.resolvePrefix() + when (filed.namePolicy) {
                Dotenv.FieldNamePolicy.UPPERCASE_WITH_UNDERSCORE -> {
                    property.simpleName.asString().uppercase()
                }

                Dotenv.FieldNamePolicy.FIELD_NAME -> {
                    property.simpleName.asString()
                }

                Dotenv.FieldNamePolicy.SPECIFIC -> {
                    filed.specific
                }
            }
        }
    }

    @OptIn(KspExperimental::class)
    fun resolveDotenvClassDescriptor(ksClassDeclaration: KSClassDeclaration): DotenvClassDescriptor {
        val dotenvList = ksClassDeclaration.getAnnotationsByType(Dotenv::class).toList()
        return DotenvClassDescriptor(
            ksClassDeclaration,
            dotenvList.first(),
            ksClassDeclaration.getDeclaredProperties().map { resolveFieldDescriptor(it) }.toList()
        )
    }

    @OptIn(KspExperimental::class)
    private fun resolveFieldDescriptor(ksPropertyDeclaration: KSPropertyDeclaration): DotenvFieldDescriptor {
        return DotenvFieldDescriptor(
            ksPropertyDeclaration,
            ksPropertyDeclaration.getAnnotationsByType(Dotenv.Field::class).first()
        )
    }
}