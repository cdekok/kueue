package eu.kueue

import kotlin.reflect.KClass

typealias MessagesProcessor<T> = suspend (List<T>) -> Unit

interface Consumer {

    suspend fun <T : Message> subscribe(
        topic: String,
        batchSize: Int,
        listeners: List<EventListener>,
        clazz: KClass<T>,
    )

    /**
     * Start consuming messages
     */
    suspend fun start()

    /**
     * Stop consuming messages
     */
    suspend fun stop()
}

suspend inline fun <reified T : Message> Consumer.subscribe(
    topic: String,
    batchSize: Int,
    listeners: List<EventListener>,
) = subscribe(topic, batchSize, listeners, T::class)
