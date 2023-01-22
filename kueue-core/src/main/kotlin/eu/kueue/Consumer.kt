package eu.kueue

import kotlin.reflect.KClass

typealias MessagesProcessor<T> = suspend (List<T>) -> Unit

interface Consumer {

    suspend fun <T : Message> subscribe(
        topic: String,
        amount: Int,
        clazz: KClass<T>,
        callBack: MessagesProcessor<T>,
    )

    suspend fun subscribe(
        topic: String,
        amount: Int,
        listeners: List<EventListener>,
    )
}

suspend inline fun <reified T : Message> Consumer.subscribe(
    topic: String,
    batchSize: Int,
    noinline callBack: MessagesProcessor<T>,
) =
    subscribe(topic, batchSize, T::class, callBack)
