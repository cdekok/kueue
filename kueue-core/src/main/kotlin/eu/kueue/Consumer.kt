package eu.kueue

import kotlin.reflect.KClass

typealias ProcessMessage<T> = (List<T>) -> Unit

interface Consumer {

    suspend fun <T : Message> subscribe(
        topic: String,
        amount: Int,
        clazz: KClass<T>,
        callBack: ProcessMessage<T>,
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
    noinline callBack: ProcessMessage<T>,
) =
    subscribe(topic, batchSize, T::class, callBack)
