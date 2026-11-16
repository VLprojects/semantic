# semantic-ksp-helper

Extensions of the API of [KSP](https://kotlinlang.org/docs/ksp-overview.html) that the processors of the other
families share. KSP comes from the processor that uses them: the module takes it as `compileOnly`.

The helpers are in `pro.vlprojects.semantic.ksp.helper`, a file per type they extend: `KSAnnotatedHelper.kt`,
`KSTypeReferenceHelper.kt`.

| Extends | Helper | What it gives |
| --- | --- | --- |
| `KSAnnotated` | `getAnnotation(KClass)`, `hasAnnotation(KClass)` | the annotation of a class, also under a type alias |
| `KSAnnotation` | `getAnnotationClass()` | the class of the annotation, a type alias expanded |
| `KSAnnotation` | `getClassArray(attributeName)` | the classes of an array attribute, a single class of Java too |
| `KSTypeReference` | `getAllInvolvedClasses()` | the classes of a type: arguments, type aliases, bounds of type parameters |
| `KSClassDeclaration` | `getNestedClasses()` | the classes nested directly in the class |
| `KSDeclaration` | `getShortName()`, `getQualifiedName()` | the name without the package, `FileModule.SharedClient`, and the full one |
| `KSDeclaration` | `isSynthetic()` | a member the compiler generates: of a data class, of a delegate |

`getAnnotationsByType` and `isAnnotationPresent` of KSP compare the short name of an annotation and miss one under
a type alias; `getAnnotation` compares the qualified name of the class the alias stands for.

```kotlin
declaration.hasAnnotation(Facade::class)
declaration.getAnnotation(Facade::class)?.getClassArray("subsystems")
property.type.getAllInvolvedClasses() // Map<String, List<FileEntity>>: String, FileEntity, List, Map
```

A bound that leads back to its own parameter, as `T : Comparable<T>`, is walked once.
