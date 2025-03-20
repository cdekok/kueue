package eu.kueue

import kotlin.reflect.*
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.jvmErasure

interface EventListener

data class CallableListener(
    val listener: EventListener,
    val method: KFunction<*>,
    val firstParameter: Parameter,
) {
    suspend fun processMessage(message: Message) {
        method.callSuspend(listener, message)
    }

    suspend fun processMessages(messages: List<Message>) {
        method.callSuspend(listener, messages)
    }
}

@JvmInline
value class Parameter(val value: KType) {
    fun type(): KClassifier? = value.classifier
    fun isList(): Boolean = value.classifier == List::class
    fun listType(): KClassifier? = value.arguments.firstOrNull()?.type?.classifier
}

fun List<EventListener>.eventHandlers() =
    this.flatMap { listener ->
        listener.eventHandlers()
    }

fun EventListener.eventHandlers(): List<CallableListener> =
    this::class.declaredFunctions.filter { func ->
        func.annotations.any { annotation ->
            annotation is EventHandler
        }
    }.map { method ->
        CallableListener(
            listener = this,
            method = method,
            firstParameter = Parameter(method.firstParameterType()),
        )
    }

fun KFunction<*>.firstParameterType(): KType =
    parameters.first { it.kind == KParameter.Kind.VALUE }.type
