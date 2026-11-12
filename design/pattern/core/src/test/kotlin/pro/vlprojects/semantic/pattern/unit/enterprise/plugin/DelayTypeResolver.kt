package pro.vlprojects.semantic.pattern.unit.enterprise.plugin

import pro.vlprojects.semantic.pattern.behavioral.resolver.TypeNameResolver

val delayTypeResolver = listOf(FixedDelay::class, ExponentialDelay::class)
    .let { subtypes -> TypeNameResolver.of(Delay::class, subtypes) }
