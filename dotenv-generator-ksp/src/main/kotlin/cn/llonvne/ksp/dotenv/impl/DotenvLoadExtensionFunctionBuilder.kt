package cn.llonvne.ksp.dotenv.impl

import cn.llonvne.Dotenv
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName

class DotenvLoadExtensionFunctionBuilder(
    private val environment: SymbolProcessorEnvironment
) {

    private val env = "env"

    fun resolve(dotenvClassDescriptor: DotenvClassDescriptorResolver.DotenvClassDescriptor): FunSpec.Builder {
        return FunSpec.builder(dotenvClassDescriptor.loaderFunctionName)
            .receiver(Dotenv.Companion::class)
            .addCode(buildDotenvInitialization())
            .also { funSpec ->
                dotenvClassDescriptor.properties.map { loadField(dotenvClassDescriptor, it) }
                    .forEach { funSpec.addCode(it) }
            }
            .addCode(buildCode(dotenvClassDescriptor))
            .addParameter(buildDslParameter())
            .returns(dotenvClassDescriptor.className)
    }

    private fun buildDslParameter(): ParameterSpec {
        val configurationClass = ClassName.bestGuess("io.github.cdimascio.dotenv.Configuration")
        val lambdaTypeName = LambdaTypeName.get(configurationClass, returnType = UNIT)
        return ParameterSpec.builder("dsl", lambdaTypeName)
            .defaultValue("{}")
            .build()
    }

    private fun buildDotenvInitialization(): CodeBlock {
        val dotenv = MemberName("io.github.cdimascio.dotenv", "dotenv")
        return CodeBlock.builder()
            .addStatement("val %N = %M(%N)", env, dotenv, "dsl")
            .build()
    }

    private fun buildCode(dotenvClassDescriptor: DotenvClassDescriptorResolver.DotenvClassDescriptor): CodeBlock {
        return CodeBlock.builder()
            .addStatement("return %T(%L)", dotenvClassDescriptor.className, passToConstruction(dotenvClassDescriptor))
            .build()
    }

    private fun passToConstruction(dotenvClassDescriptor: DotenvClassDescriptorResolver.DotenvClassDescriptor): String {
        return buildString {
            dotenvClassDescriptor.properties.forEach {
                append("${it.property.simpleName.asString()} = ${fieldVariableName(it)},")
            }
        }
    }

    private fun fieldVariableName(fieldDescriptor: DotenvClassDescriptorResolver.DotenvFieldDescriptor): String {
        return "`${fieldDescriptor.property.qualifiedName?.asString()}`".replace(".", "$")
    }

    private fun loadField(
        classDescriptorResolver: DotenvClassDescriptorResolver.DotenvClassDescriptor,
        fieldDescriptor: DotenvClassDescriptorResolver.DotenvFieldDescriptor
    ): CodeBlock {

        val type = fieldDescriptor.resolveFieldType()
        // STRING TYPE
        if (type.declaration.qualifiedName?.asString() == "kotlin.String") {
            return CodeBlock.builder()
                .addStatement(
                    "val %N = %N[%S]",
                    fieldVariableName(fieldDescriptor),
                    env,
                    fieldDescriptor.resolveKeyName(classDescriptorResolver)
                )
                .build()
        }

        val declaration = type.declaration

        // ENUM TYPE
        if (declaration is KSClassDeclaration) {
            if (declaration.classKind == ClassKind.ENUM_CLASS) {
                return loadEnum(declaration, classDescriptorResolver, fieldDescriptor)
            }
        }

        TODO("UNSUPPORTED TYPE")

    }

    private fun loadEnum(
        enumType: KSClassDeclaration,
        classDescriptorResolver: DotenvClassDescriptorResolver.DotenvClassDescriptor,
        fieldDescriptor: DotenvClassDescriptorResolver.DotenvFieldDescriptor
    ): CodeBlock {
        return CodeBlock.builder()
            .addStatement(
                "val %N = %T.valueOf(%N[%S])",
                fieldVariableName(fieldDescriptor),
                enumType.toClassName(),
                env,
                fieldDescriptor.resolveKeyName(classDescriptorResolver)
            )
            .build()
    }
}