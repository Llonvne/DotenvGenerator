package cn.llonvne.ksp.dotenv.impl

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import java.util.*

interface DotenvFieldVariableNameProvider {
    fun provide(): String

    fun order(): Int

    companion object {
        private class FieldVariableNameDefaultProvider(
            private val ksPropertyDeclaration: KSPropertyDeclaration
        ) : DotenvFieldVariableNameProvider {
            override fun provide(): String {
                return buildString {
                    append("`")
                    append("${ksPropertyDeclaration.qualifiedName?.asString()}")
                    append("`")
                }.replace(".", "$")
            }

            override fun order(): Int {
                return 0
            }
        }

        class DotenvFieldNameProviderQueue {
            private val queue = PriorityQueue<DotenvFieldVariableNameProvider>(
                compareByDescending { it.order() }
            )

            fun addProvider(provider: DotenvFieldVariableNameProvider) {
                queue.add(provider)
            }

            fun provide(): String {
                return queue.first().provide()
            }

            fun removeProvider(provider: DotenvFieldVariableNameProvider) {
                queue.remove(provider)
            }
        }

        fun defaultProviderQueue(ksPropertyDeclaration: KSPropertyDeclaration): DotenvFieldNameProviderQueue {
            return DotenvFieldNameProviderQueue().also {
                it.addProvider(
                    FieldVariableNameDefaultProvider(
                        ksPropertyDeclaration
                    )
                )
            }
        }
    }
}