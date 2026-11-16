package pro.vlprojects.semantic.ksp.helper

import com.google.devtools.ksp.symbol.KSClassDeclaration

public fun KSClassDeclaration.findAllNestedClasses(): Sequence<KSClassDeclaration> = this.declarations
    .filterIsInstance<KSClassDeclaration>()
    .flatMap { nested ->
        nested
            .findAllNestedClasses()
            .let { deeper -> sequenceOf(nested) + deeper }
    }
