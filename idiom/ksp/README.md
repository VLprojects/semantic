# semantic-ksp-helper

Extensions of the API of [KSP](https://kotlinlang.org/docs/ksp-overview.html) that the processors of the other
families share. KSP comes from the processor that uses them: the module takes it as `compileOnly`.

The helpers are in `pro.vlprojects.semantic.ksp.helper`, a file per type they extend: `KSAnnotatedHelper.kt`,
`KSTypeReferenceHelper.kt`.

| Extends | Helper | What it gives |
| --- | --- | --- |
| `KSAnnotated` | `findAnnotation(KClass)`, `hasAnnotation(KClass)` | the annotation of a class, also under a type alias |
| `KSAnnotation` | `findAnnotationClass()` | the class of the annotation, a type alias expanded |
| `KSAnnotation` | `readArgumentValues(name)` | the values of an array attribute, a single value of Java too |
| `KSTypeReference` | `findAllInvolvedClasses()` | the classes of a type: arguments, type aliases, bounds of type parameters |
| `KSClassDeclaration` | `findAllNestedClasses()` | the nested classes at every depth, without the class itself |
| `KSDeclaration` | `readShortName()`, `readQualifiedName()` | the name without the package, `FileModule.SharedClient`, and the full one |
| `KSDeclaration` | `isSynthetic()` | a member the compiler generates: of a data class, of a delegate |

`getAnnotationsByType` and `isAnnotationPresent` of KSP compare the short name of an annotation and miss one under
a type alias; `findAnnotation` compares the qualified name of the class the alias stands for.

```kotlin
declaration.hasAnnotation(Facade::class)
declaration.findAnnotation(Facade::class)?.readArgumentValues("subsystems")
property.type.findAllInvolvedClasses() // Map<String, List<FileEntity>>: String, FileEntity, List, Map
```

A bound that leads back to its own parameter, as `T : Comparable<T>`, is walked once.
