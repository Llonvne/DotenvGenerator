package cn.llonvne.ksp.dotenv.type

import cn.llonvne.ksp.dotenv.impl.DotenvClassDescriptorResolver
import com.squareup.kotlinpoet.CodeBlock

interface FieldLoader {
    companion object {
        const val env: String = "env"
    }

    fun load(
        classDescriptor: DotenvClassDescriptorResolver.DotenvClassDescriptor,
        fieldDescriptor: DotenvClassDescriptorResolver.DotenvFieldDescriptor,
        prefix: String,
        parentFieldDescriptor: DotenvClassDescriptorResolver.DotenvFieldDescriptor? = null
    ): CodeBlock

    fun support(
        classDescriptor: DotenvClassDescriptorResolver.DotenvClassDescriptor,
        fieldDescriptor: DotenvClassDescriptorResolver.DotenvFieldDescriptor
    ): Boolean

    fun order(): Int
}