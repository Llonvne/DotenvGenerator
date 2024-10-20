package cn.llonvne.ksp.dotenv.type

import cn.llonvne.ksp.dotenv.impl.DotenvClassDescriptorResolver
import cn.llonvne.ksp.dotenv.impl.DotenvLoadExtensionFunctionBuilder
import com.squareup.kotlinpoet.CodeBlock

interface FieldLoader {
    companion object {
        const val env: String = "env"
    }

    fun load(
        loadFieldContext: DotenvLoadExtensionFunctionBuilder.LoadFieldContext
    ): CodeBlock

    fun support(
        classDescriptor: DotenvClassDescriptorResolver.DotenvClassDescriptor,
        fieldDescriptor: DotenvClassDescriptorResolver.DotenvFieldDescriptor
    ): Boolean

    fun order(): Int
}