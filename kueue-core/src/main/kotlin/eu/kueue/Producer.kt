package eu.kueue

import kotlin.reflect.KClass

interface Producer {
    suspend fun <T : Message> send(topic: String, message: T, clazz: KClass<T>)
}

suspend inline fun <reified T : Message> Producer.send(topic: String, message: T) =
    send(topic, message, T::class)
