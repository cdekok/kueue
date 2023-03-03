package eu.kueue.serializer.xstream

import com.thoughtworks.xstream.XStream
import eu.kueue.Message
import eu.kueue.MessageSerializer
import kotlin.reflect.KClass

class XstreamMessageSerializer(
    private val xstream: XStream,
) : MessageSerializer {

    override suspend fun <T : Message> serialize(message: T, clazz: KClass<T>): String =
        // always encode against interface instead of generic
        // to enforce the type property in json required during decoding
        xstream.toXML(message)

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Message> deserialize(serialized: String, clazz: KClass<T>): T =
        xstream.fromXML(serialized) as T
}
