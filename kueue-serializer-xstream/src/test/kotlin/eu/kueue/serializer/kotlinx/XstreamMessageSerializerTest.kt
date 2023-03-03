package eu.kueue.serializer.kotlinx

import com.thoughtworks.xstream.XStream
import eu.kueue.Message
import eu.kueue.deserialize
import eu.kueue.serialize
import eu.kueue.serializer.xstream.XstreamMessageSerializer
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

data class TestMessage(
    val id: Int,
    val title: String,
) : Message

class XstreamMessageSerializerTest {

    private val serde = XstreamMessageSerializer(
        xstream = XStream().apply {
            allowTypes(arrayOf(TestMessage::class.java))
        }
    )

    private val message = TestMessage(
        id = 1,
        title = "test",
    )

    private val xml = """
            <eu.kueue.serializer.kotlinx.TestMessage>
              <id>1</id>
              <title>test</title>
            </eu.kueue.serializer.kotlinx.TestMessage>
    """.trimIndent()

    @Test
    fun `serialize`() = runBlocking {
        val serialized = serde.serialize(message)
        assertEquals(xml, serialized)
    }

    @Test
    fun `deserialize`() = runBlocking {
        val deserialized: Message = serde.deserialize(xml)
        assertEquals(message, deserialized)
    }
}
