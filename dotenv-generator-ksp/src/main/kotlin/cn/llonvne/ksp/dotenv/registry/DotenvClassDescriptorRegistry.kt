package cn.llonvne.ksp.dotenv.registry

import cn.llonvne.ksp.dotenv.impl.DotenvClassDescriptorResolver

data object DotenvClassDescriptorRegistry {
    private val _registry: MutableMap<String, DotenvClassDescriptorResolver.DotenvClassDescriptor> = mutableMapOf()

    fun register(dotenvClassDescriptor: DotenvClassDescriptorResolver.DotenvClassDescriptor) {
        _registry[dotenvClassDescriptor.classDeclaration.qualifiedName?.asString()!!] = dotenvClassDescriptor
    }

    fun get(qualifiedName: String): DotenvClassDescriptorResolver.DotenvClassDescriptor? {
        return _registry[qualifiedName]
    }
}