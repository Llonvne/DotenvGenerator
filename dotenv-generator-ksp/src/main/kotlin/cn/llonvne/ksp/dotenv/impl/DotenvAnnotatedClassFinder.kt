package cn.llonvne.ksp.dotenv.impl

import cn.llonvne.Dotenv
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration

class DotenvAnnotatedClassFinder(
   private val environment: SymbolProcessorEnvironment
) {
    fun find(resolver: Resolver): List<KSClassDeclaration> {
        return resolver.getSymbolsWithAnnotation(
            Dotenv::class.qualifiedName!!
        ).filterIsInstance<KSClassDeclaration>().toList()
    }
}