package eu.kueue

import kotlin.reflect.KClass

interface MessageSerializer {

    suspend fun <T : Message> serialize(message: T, clazz: KClass<T>): String

    suspend fun <T : Message> deserialize(serialized: String, clazz: KClass<T>): T
}

suspend inline fun <reified T : Message> MessageSerializer.serialize(message: T): String =
    serialize(message, T::class)

suspend inline fun <reified T : Message> MessageSerializer.deserialize(serialized: String): T =
    deserialize(serialized, T::class)
