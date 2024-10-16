package cn.llonvne.ksp.dotenv.type

import cn.llonvne.Dotenv
import cn.llonvne.ksp.dotenv.impl.DotenvClassDescriptorResolver
import cn.llonvne.ksp.dotenv.impl.DotenvClassDescriptorResolver.DotenvClassDescriptor
import cn.llonvne.ksp.dotenv.impl.DotenvClassDescriptorResolver.DotenvFieldDescriptor
import cn.llonvne.ksp.dotenv.impl.DotenvFieldVariableNameProvider
import cn.llonvne.ksp.dotenv.registry.DotenvClassDescriptorRegistry
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.squareup.kotlinpoet.CodeBlock

class RecursiveFieldLoader(
    private val loadField: (DotenvClassDescriptor, DotenvFieldDescriptor, String, DotenvFieldDescriptor) -> CodeBlock
) : FieldLoader {

    class RecursiveFieldVariableNameProvider(
        private val fieldDescriptor: DotenvFieldDescriptor,
        private val parentFieldDescriptor: DotenvFieldDescriptor
    ) : DotenvFieldVariableNameProvider {
        override fun provide(): String {
            return buildString {
                append("`")
                append("${fieldDescriptor.property.qualifiedName?.asString()}")
                append("_${parentFieldDescriptor.property.simpleName.asString()}")
                append("`")
            }.replace(".", "$")
        }

        override fun order(): Int {
            return 100
        }
    }

    private fun <R> DotenvFieldDescriptor.recursiveFieldVariableNameProviderScope(
        parentFieldDescriptor: DotenvFieldDescriptor,
        action: () -> R
    ): R {
        val recursiveFieldVariableNameProvider = RecursiveFieldVariableNameProvider(this, parentFieldDescriptor)
        this.nameProvider.addProvider(recursiveFieldVariableNameProvider)
        val ret = action()
        this.nameProvider.removeProvider(recursiveFieldVariableNameProvider)
        return ret
    }

    override fun load(
        classDescriptor: DotenvClassDescriptor,
        fieldDescriptor: DotenvFieldDescriptor,
        prefix: String,
        parentFieldDescriptor: DotenvFieldDescriptor?
    ): CodeBlock {

        val filedClassDescriptor =
            DotenvClassDescriptorRegistry.get(
                fieldDescriptor.property.type.resolve().declaration.qualifiedName?.asString()!!
            )!!

        val codeBlocks = filedClassDescriptor.properties.map {
            it.recursiveFieldVariableNameProviderScope(fieldDescriptor) {
                loadField.invoke(filedClassDescriptor, it, prefix + classDescriptor.resolvePrefix(), fieldDescriptor)
            }
        }

        return CodeBlock.builder()
            .also { code ->
                codeBlocks.forEach { code.add(it) }
            }
            .add(recursiveBuildCode(filedClassDescriptor, fieldDescriptor, prefix))
            .build()
    }

    private fun recursiveBuildCode(
        fieldClassDescriptor: DotenvClassDescriptor,
        fieldDescriptor: DotenvFieldDescriptor,
        prefix: String
    ): CodeBlock {
        return CodeBlock.builder()
            .addStatement(
                "val %N = %T(%L)",
                fieldDescriptor.nameProvider.provide(),
                fieldClassDescriptor.className,
                passToConstruction(fieldClassDescriptor, fieldDescriptor)
            )
            .build()
    }

    private fun passToConstruction(
        classDescriptor: DotenvClassDescriptor,
        fieldDescriptor: DotenvFieldDescriptor
    ): String {
        return buildString {
            classDescriptor.properties.forEach {
                it.recursiveFieldVariableNameProviderScope(fieldDescriptor) {
                    append("${it.property.simpleName.asString()} = ${it.nameProvider.provide()},")
                }
            }
        }
    }

    @OptIn(KspExperimental::class)
    override fun support(
        classDescriptor: DotenvClassDescriptor,
        fieldDescriptor: DotenvFieldDescriptor
    ): Boolean {
        return fieldDescriptor.filed.recursive && fieldDescriptor.property.type.resolve().declaration.isAnnotationPresent(
            Dotenv::class
        )
    }

    override fun order(): Int {
        return Int.MAX_VALUE / 2
    }
}