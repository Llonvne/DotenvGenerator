package cn.llonvne.ksp.dotenv.impl

import java.util.*

interface DotnetFieldKeyNameProvider {
    fun provide(): String

    fun order(): Int

    companion object {
        private class DefaultFieldKeyNameProvider : DotnetFieldKeyNameProvider {
            override fun provide(): String {
                TODO("Not yet implemented")
            }

            override fun order(): Int {
                TODO("Not yet implemented")
            }
        }

        class DotnetFieldKeyNameProviderQueue {
            private val queue = PriorityQueue<DotnetFieldKeyNameProvider>(
                compareByDescending { it.order() }
            )

            fun addProvider(provider: DotnetFieldKeyNameProvider) {
                queue.add(provider)
            }

            fun provide(): String {
                return queue.first().provide()
            }

            fun removeProvider(provider: DotnetFieldKeyNameProvider) {
                queue.remove(provider)
            }
        }

        fun defaultKeyNameProviderQueue(): DotnetFieldKeyNameProviderQueue {
            return DotnetFieldKeyNameProviderQueue().also { it.addProvider(DefaultFieldKeyNameProvider()) }
        }
    }
}