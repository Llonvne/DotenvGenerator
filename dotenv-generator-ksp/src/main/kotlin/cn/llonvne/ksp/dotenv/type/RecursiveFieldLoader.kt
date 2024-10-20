package cn.llonvne.ksp.dotenv.type

import cn.llonvne.Dotenv
import cn.llonvne.ksp.dotenv.impl.DotenvClassDescriptorResolver
import cn.llonvne.ksp.dotenv.impl.DotenvClassDescriptorResolver.DotenvClassDescriptor
import cn.llonvne.ksp.dotenv.impl.DotenvClassDescriptorResolver.DotenvFieldDescriptor
import cn.llonvne.ksp.dotenv.impl.DotenvFieldVariableNameProvider
import cn.llonvne.ksp.dotenv.impl.DotenvLoadExtensionFunctionBuilder
import cn.llonvne.ksp.dotenv.impl.DotenvLoadExtensionFunctionBuilder.LoadFieldContext
import cn.llonvne.ksp.dotenv.impl.DotnetFieldKeyNameProvider
import cn.llonvne.ksp.dotenv.registry.DotenvClassDescriptorRegistry
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.CodeBlock
import org.intellij.lang.annotations.Identifier
import java.util.UUID

class RecursiveFieldLoader(
    private val loadField: (LoadFieldContext) -> CodeBlock
) : FieldLoader {

    class RecursiveFieldVariableNameProvider(
        private val lastFieldContext: LoadFieldContext,
        private val identifier: UUID
    ) : DotenvFieldVariableNameProvider {
        override fun provide(): String {
            return buildString {
                append("`")
                append("${lastFieldContext.fieldDescriptor.property.qualifiedName?.asString()}")
                append("_${lastFieldContext.lastContext?.fieldDescriptor?.property?.simpleName?.asString()}")
                append("_${identifier.toString().substring(0..6)}")
                append("`")
            }.replace(".", "$")
        }

        override fun order(): Int {
            return 100
        }
    }

    private fun <R> recursiveFieldVariableNameProviderScope(
        identifier: UUID,
        lastFieldContext: LoadFieldContext, action: () -> R
    ): R {
        val recursiveFieldVariableNameProvider = RecursiveFieldVariableNameProvider(lastFieldContext, identifier)
        lastFieldContext.fieldDescriptor.nameProvider.addProvider(recursiveFieldVariableNameProvider)
        val ret = action()
        lastFieldContext.fieldDescriptor.nameProvider.removeProvider(recursiveFieldVariableNameProvider)
        return ret
    }

    class RecursiveFieldKeyNameProvider(
        private val fieldDescriptor: DotenvFieldDescriptor
    ) : DotnetFieldKeyNameProvider {
        override fun provide(): String {
            return fieldDescriptor.property.simpleName.getShortName()
        }
    }

    override fun load(
        loadFieldContext: LoadFieldContext
    ): CodeBlock = with(loadFieldContext) {

        val identifier = UUID.randomUUID()

        val filedClassDescriptor = DotenvClassDescriptorRegistry.get(
            fieldDescriptor.property.type.resolve().declaration.qualifiedName?.asString()!!
        )!!

        val contextMap: MutableMap<KSPropertyDeclaration, LoadFieldContext> = mutableMapOf()

        val codeBlocks = filedClassDescriptor.properties.map {

            val keyProvider = RecursiveFieldKeyNameProvider(fieldDescriptor)

            it.keyProvider.addProvider(keyProvider)

            val newContext = LoadFieldContext(
                filedClassDescriptor, it, prefix + classDescriptor.resolvePrefix(), loadFieldContext
            )

            contextMap[it.property] = newContext

            val ret = recursiveFieldVariableNameProviderScope(identifier, newContext) {
                loadField.invoke(newContext)
            }

            it.keyProvider.removeProvider(keyProvider)

            ret
        }

        return CodeBlock.builder().also { code ->
            codeBlocks.forEach { code.add(it) }
        }.add(recursiveBuildCode(identifier, filedClassDescriptor, fieldDescriptor, contextMap)).build()
    }

    private fun recursiveBuildCode(
        identifier: UUID,
        fieldClassDescriptor: DotenvClassDescriptor,
        fieldDescriptor: DotenvFieldDescriptor,
        contextMap: Map<KSPropertyDeclaration, LoadFieldContext>
    ): CodeBlock {
        return CodeBlock.builder().addStatement(
            "val %N = %T(%L)",
            fieldDescriptor.nameProvider.provide(),
            fieldClassDescriptor.className,
            passToConstruction(identifier, fieldClassDescriptor, contextMap)
        ).build()
    }

    private fun passToConstruction(
        identifier: UUID,
        classDescriptor: DotenvClassDescriptor,
        contextMap: Map<KSPropertyDeclaration, LoadFieldContext>
    ): String {
        return buildString {
            classDescriptor.properties.forEach {
                recursiveFieldVariableNameProviderScope(identifier, contextMap[it.property]!!) {
                    append("${it.property.simpleName.asString()} = ${it.nameProvider.provide()},")
                }
            }
        }
    }

    @OptIn(KspExperimental::class)
    override fun support(
        classDescriptor: DotenvClassDescriptor, fieldDescriptor: DotenvFieldDescriptor
    ): Boolean {
        return fieldDescriptor.filed.recursive && fieldDescriptor.property.type.resolve().declaration.isAnnotationPresent(
            Dotenv::class
        )
    }

    override fun order(): Int {
        return Int.MAX_VALUE / 2
    }
}