package eu.kueue

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.jvmErasure

interface EventListener

data class CallableListener(
    val listener: EventListener,
    val method: KFunction<*>,
    val firstArgumentType: KClass<*>,
) {
    fun call(message: Message) {
        method.call(listener, message)
    }
}

fun List<EventListener>.eventHandlers() =
    this.flatMap { listener ->
        listener::class.declaredFunctions.filter { func ->
            func.annotations.any { annotation ->
                annotation is EventHandler
            }
        }.map { method ->
            CallableListener(
                listener = listener,
                method = method,
                firstArgumentType = method.firstArgumentType()
            )
        }
    }

private fun KFunction<*>.firstArgumentType(): KClass<*> =
    parameters.first {
        it.kind == KParameter.Kind.VALUE
    }.type.jvmErasure
