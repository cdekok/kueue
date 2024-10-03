package eu.kueue.serializer.kotlinx

import eu.kueue.Message
import eu.kueue.MessageSerializer
import kotlinx.serialization.*
import kotlin.reflect.KClass

class KotlinxMessageSerializer(
    private val serde: StringFormat,
) : MessageSerializer {

    override suspend fun <T : Message> serialize(message: T, clazz: KClass<T>): String =
        // always encode against interface instead of generic
        // to enforce the type property in json required during decoding
        serde.encodeToString<Message>(message)

    override suspend fun <T : Message> deserialize(serialized: String, clazz: KClass<T>): T =
        serde.decodeFromString(PolymorphicSerializer(clazz), serialized)
}
