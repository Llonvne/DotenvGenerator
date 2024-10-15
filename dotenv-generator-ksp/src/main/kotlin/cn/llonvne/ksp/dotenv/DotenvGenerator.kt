package cn.llonvne.ksp.dotenv

import cn.llonvne.ksp.dotenv.impl.DotenvAnnotatedClassFinder
import cn.llonvne.ksp.dotenv.impl.DotenvClassDescriptorResolver
import cn.llonvne.ksp.dotenv.impl.DotenvLoadExtensionFunctionBuilder
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo

class DotenvGenerator(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    private val annotatedClassFinder = DotenvAnnotatedClassFinder(environment)

    private val annotationGetter = DotenvClassDescriptorResolver(environment)

    private val loadExtensionFunctionBuilder = DotenvLoadExtensionFunctionBuilder(environment)

    private val dotenvExtensionFunctionFileSpec = FileSpec.builder("cn.llonvne", "dotenv")

    override fun finish() {
        dotenvExtensionFunctionFileSpec.build().writeTo(environment.codeGenerator, true)
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {

        val classes = annotatedClassFinder.find(resolver)

        val dotenvClassDescriptors = classes.map { annotationGetter.resolveDotenvClassDescriptor(it) }

        dotenvClassDescriptors.map { loadExtensionFunctionBuilder.resolve(it) }.forEach {
            dotenvExtensionFunctionFileSpec.addFunction(it.build())
        }

        return emptyList()
    }
}