package com.typewritermc.engine.minestom.loader

import com.typewritermc.core.serialization.DataSerializer
import com.typewritermc.engine.minestom.loader.serializers.*
import org.koin.core.module.Module
import org.koin.core.qualifier.named

fun Module.dataSerializers() {
    single<DataSerializer<*>>(named("closedRange")) { ClosedRangeSerializer() }
    single<DataSerializer<*>>(named("color")) { ColorSerializer() }
    single<DataSerializer<*>>(named("cronExpression")) { CronExpressionSerializer() }
    single<DataSerializer<*>>(named("duration")) { DurationSerializer() }
    single<DataSerializer<*>>(named("entryReference")) { EntryReferenceSerializer() }
    single<DataSerializer<*>>(named("optional")) { OptionalSerializer() }
    single<DataSerializer<*>>(named("potionEffectType")) { PotionEffectTypeSerializer() }
    single<DataSerializer<*>>(named("skinProperty")) { SkinPropertySerializer() }
    single<DataSerializer<*>>(named("soundId")) { SoundIdSerializer() }
    single<DataSerializer<*>>(named("soundSource")) { SoundSourceSerializer() }
    single<DataSerializer<*>>(named("vector")) { VectorSerializer() }
    single<DataSerializer<*>>(named("world")) { WorldSerializer() }
}