package cn.llonvne.ksp.dotenv.type

import cn.llonvne.ksp.dotenv.impl.DotenvClassDescriptorResolver
import cn.llonvne.ksp.dotenv.type.FieldLoader.Companion.env
import com.squareup.kotlinpoet.CodeBlock

class StringFieldLoader : FieldLoader {

    override fun order(): Int {
        return 0
    }

    override fun load(
        classDescriptor: DotenvClassDescriptorResolver.DotenvClassDescriptor,
        fieldDescriptor: DotenvClassDescriptorResolver.DotenvFieldDescriptor,
        prefix: String,
        parentFieldDescriptor: DotenvClassDescriptorResolver.DotenvFieldDescriptor?
    ): CodeBlock {
        return CodeBlock.builder()
            .addStatement(
                "val %N = %N[%S]",
                fieldDescriptor.nameProvider.provide(),
                env,
                fieldDescriptor.resolveKeyName(prefix, classDescriptor, parentFieldDescriptor)
            )
            .build()
    }

    override fun support(
        classDescriptor: DotenvClassDescriptorResolver.DotenvClassDescriptor,
        fieldDescriptor: DotenvClassDescriptorResolver.DotenvFieldDescriptor
    ): Boolean {
        return fieldDescriptor.resolveFieldType().declaration.qualifiedName?.asString() == "kotlin.String"
    }
}