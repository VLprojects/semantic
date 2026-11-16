# semantic-ksp-helper

Extensions of the API of [KSP](https://kotlinlang.org/docs/ksp-overview.html) that the processors of the other
families share. KSP comes from the processor that uses them: the module takes it as `compileOnly`.

The helpers are in `pro.vlprojects.semantic.ksp.helper`, a file per type they extend: `KSAnnotatedHelper.kt`,
`KSTypeReferenceHelper.kt`.

| Extends | Helper | What it gives |
| --- | --- | --- |
| `KSAnnotated` | `getAnnotation(KClass)`, `hasAnnotation(KClass)` | the annotation of a class, also under a type alias |
| `KSAnnotation` | `resolveClassDeclaration()` | the declaration of the class of the annotation, a type alias expanded |
| `KSAnnotation` | `getTypeValues(argumentName)` | the types an argument holds, `Ledger::class` or `Ledger.class`, a single one of Java too |
| `KSTypeReference` | `getReferencedClasses()` | the classes a type refers to: its own, of arguments, type aliases, bounds of type parameters |
| `KSClassDeclaration` | `getDeclaredClasses()` | the classes declared directly in the class |
| `KSDeclaration` | `getShortName()`, `getQualifiedName()` | the name without the package, `FileModule.SharedClient`, and the full one |
| `KSDeclaration` | `isSynthetic()` | a member the compiler generates: of a data class, of a delegate |

`getAnnotationsByType` and `isAnnotationPresent` of KSP compare the short name of an annotation and miss one under
a type alias; `getAnnotation` compares the qualified name of the class the alias stands for.

```kotlin
declaration.hasAnnotation(Facade::class)
declaration.getAnnotation(Facade::class)?.getTypeValues("subsystems")
property.type.getReferencedClasses() // Map<String, List<FileEntity>>: String, FileEntity, List, Map
```

A bound that leads back to its own parameter, as `T : Comparable<T>`, is walked once.
