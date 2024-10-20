package cn.llonvne.ksp.dotenv.type

import cn.llonvne.ksp.dotenv.impl.DotenvClassDescriptorResolver
import cn.llonvne.ksp.dotenv.impl.DotenvLoadExtensionFunctionBuilder
import cn.llonvne.ksp.dotenv.type.FieldLoader.Companion.env
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ksp.toClassName

class EnumFieldLoader : FieldLoader {
    override fun load(
        loadFieldContext: DotenvLoadExtensionFunctionBuilder.LoadFieldContext
    ): CodeBlock = with(loadFieldContext) {
        val declaration = fieldDescriptor.property.type.resolve().declaration as KSClassDeclaration
        return CodeBlock.builder()
            .addStatement(
                "val %N = %T.valueOf(%N[%S])",
                fieldDescriptor.nameProvider.provide(),
                declaration.toClassName(),
                env,
                fieldDescriptor.resolveKeyName(prefix, classDescriptor, lastContext?.fieldDescriptor)
            )
            .build()
    }

    override fun support(
        classDescriptor: DotenvClassDescriptorResolver.DotenvClassDescriptor,
        fieldDescriptor: DotenvClassDescriptorResolver.DotenvFieldDescriptor
    ): Boolean {
        val declaration = fieldDescriptor.property.type.resolve().declaration
        if (declaration is KSClassDeclaration) {
            if (declaration.classKind == ClassKind.ENUM_CLASS) {
                return true
            }
        }
        return false
    }

    override fun order(): Int {
        return 1
    }
}