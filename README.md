# Kueue

## tl:dr;
Kueue is a persistent job queue in Kotlin to offload tasks to be processed in the background

## Quick start

To view the full source of the example look in the [example module](/kueue-example-pg/src/main/kotlin/eu/kueue/example)

### Producer

Create a producer and send messages to process

```kotlin
// postgres client
val db = PgPool.pool(
    PgConnectOptions.fromEnv(),
    PoolOptions().setMaxSize(10),
)

// kotlinx json serializer
val serializer = KotlinxMessageSerializer(
    Json {
        classDiscriminator = "type"
        serializersModule = SerializersModule {
            polymorphic(Message::class) {
                subclass(RecordCreated::class)
            }
        }
    }
)

// postgres producer
val producer: Producer = PgProducer(
    client = db,
    serializer = serializer,
)

// message to send
@Serializable
data class RecordCreated(
    val id: Int,
    val title: String,
) : Message

val message = RecordCreated(
    id = 123,
    title = "test title $it",
)

// send the message
producer.send(
    topic = DEFAULT_TOPIC,
    message = message,
)
```

### Consumer

Setup a listener to process the events

```kotlin
class TestListener : EventListener {
    @EventHandler
    fun on(event: RecordCreated) {
        logger.info { "handle record created $event" }
    }
}

val listeners = listOf(
    TestListener()
)

val consumer = PgConsumer(
    client = pgPool(),
    serializer = kotlinXSerializer(),
)

runBlocking {
    consumer.subscribe(
        topic = "records",
        amount = 10,
        listeners = listeners,
    )
}
```

## Run the example module

Set the correct java version with [sdk](https://sdkman.io/)

```shell
sdk env
```

Start postgres

```shell
docker-compose -f ./kueue-example-pg/docker-compose.yml up
```

Send some messages to be processed

```shell
./kueue-example-pg/run producer --amount=10000
```

Consume messages in a different process

```shell
./kueue-example-pg/run consumer
```

## Development

Set the correct java version with [sdk](https://sdkman.io/)

```shell
sdk env
```

Run code formatter with detekt

```shell
./gradlew format
```

Check code formatting

```shell
./gradlew detekt`
```

Run all tests

```shell
./gradlew test
```

Run all tests & formatting rules

```shell
./gradlew check
```

Check for dependency updates

```shell
./gradlew dependencyUpdates`
```
