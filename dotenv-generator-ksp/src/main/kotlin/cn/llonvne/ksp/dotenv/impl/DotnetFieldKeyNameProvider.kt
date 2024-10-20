package cn.llonvne.ksp.dotenv.impl

import cn.llonvne.Dotenv
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

interface DotnetFieldKeyNameProvider {
    fun provide(): String

    companion object {

        fun resolvePrefix(dotenv: Dotenv): String {
            return if (dotenv.prefix == "") {
                ""
            } else {
                dotenv.prefix + dotenv.prefixNameSpilt
            }
        }

        private class DefaultFieldKeyNameProvider(
            private val filed: Dotenv.Field,
            private val property: KSPropertyDeclaration
        ) : DotnetFieldKeyNameProvider {
            override fun provide(): String {
                return buildString {
                    append(
                        when (filed.namePolicy) {
                            Dotenv.FieldNamePolicy.UPPERCASE_WITH_UNDERSCORE -> {
                                property.simpleName.getShortName().uppercase()
                            }

                            Dotenv.FieldNamePolicy.FIELD_NAME -> {
                                property.simpleName.getShortName()
                            }

                            Dotenv.FieldNamePolicy.SPECIFIC -> {
                                filed.specific
                            }
                        }
                    )
                }
            }
        }

        class DotnetFieldPrefixKeyNameProvider(private val dotenv: Dotenv) : DotnetFieldKeyNameProvider {
            override fun provide(): String = dotenv.prefix
        }

        class DotnetFieldKeyNameProviderQueue {
            private val keyNames = mutableListOf<DotnetFieldKeyNameProvider>()

            fun addProvider(provider: DotnetFieldKeyNameProvider) {
                keyNames.add(provider)
            }


            fun provide(loadFieldContext: DotenvLoadExtensionFunctionBuilder.LoadFieldContext): String {
                return loadFieldContext.firstContext().classDescriptor.resolvePrefix() + keyNames.reversed()
                    .joinToString(".") { it.provide() }
            }

            fun removeProvider(provider: DotnetFieldKeyNameProvider) {
                keyNames.remove(provider)
            }
        }

        fun defaultKeyNameProviderQueue(
            field: Dotenv.Field,
            property: KSPropertyDeclaration
        ): DotnetFieldKeyNameProviderQueue {
            return DotnetFieldKeyNameProviderQueue().also {
                it.addProvider(
                    DefaultFieldKeyNameProvider(
                        field,
                        property
                    )
                )
            }
        }
    }
}