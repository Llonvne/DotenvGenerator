package cn.llonvne.ksp.dotenv.impl

import cn.llonvne.Dotenv
import cn.llonvne.ksp.dotenv.impl.DotenvClassDescriptorResolver.DotenvClassDescriptor
import cn.llonvne.ksp.dotenv.impl.DotenvClassDescriptorResolver.DotenvFieldDescriptor
import cn.llonvne.ksp.dotenv.type.EnumFieldLoader
import cn.llonvne.ksp.dotenv.type.RecursiveFieldLoader
import cn.llonvne.ksp.dotenv.type.StringFieldLoader
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.squareup.kotlinpoet.*

class DotenvLoadExtensionFunctionBuilder(
    private val environment: SymbolProcessorEnvironment
) {

    private val env = "env"

    fun resolve(dotenvClassDescriptor: DotenvClassDescriptor): FunSpec.Builder {
        return FunSpec.builder(dotenvClassDescriptor.loaderFunctionName)
            .receiver(Dotenv.Companion::class)
            .addCode(buildDotenvInitialization())
            .also { funSpec ->
                dotenvClassDescriptor.properties.map { loadField(LoadFieldContext(dotenvClassDescriptor, it)) }
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

    private fun buildCode(dotenvClassDescriptor: DotenvClassDescriptor): CodeBlock {
        return CodeBlock.builder()
            .addStatement("return %T(%L)", dotenvClassDescriptor.className, passToConstruction(dotenvClassDescriptor))
            .build()
    }

    private fun passToConstruction(dotenvClassDescriptor: DotenvClassDescriptor): String {
        return buildString {
            dotenvClassDescriptor.properties.forEach {
                append("${it.property.simpleName.asString()} = ${it.nameProvider.provide()},")
            }
        }
    }

    private val normalFieldLoader = listOf(StringFieldLoader(), EnumFieldLoader())
    private val recursiveFieldLoader = RecursiveFieldLoader(::loadField)
    private val fieldLoader = (normalFieldLoader + recursiveFieldLoader).sortedBy { it.order() }

    data class LoadFieldContext(
        val classDescriptor: DotenvClassDescriptor,
        val fieldDescriptor: DotenvFieldDescriptor,
        val prefix: String = "",
        val lastContext: LoadFieldContext? = null,
    ) {
        fun firstContext(): LoadFieldContext {
            var first = this
            while (first.lastContext != null){
                first = first.lastContext!!
            }
            return first
        }
    }

    private fun loadField(
        context: LoadFieldContext
    ): CodeBlock {
        with(context) {
            for (loader in fieldLoader) {
                if (loader.support(classDescriptor, fieldDescriptor)) {
                    return loader.load(context)
                }
            }
        }
        TODO("UNSUPPORTED TYPE ")
    }
}