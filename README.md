# Kueue

[![](https://jitpack.io/v/cdekok/kueue.svg)](https://jitpack.io/#cdekok/kueue)

## tl:dr;
Kueue is a persistent job queue in Kotlin to offload tasks to be processed in the background,
with at-least-once delivery.

## Table of contents

<!-- TOC -->
* [Kueue](#kueue)
  * [tl:dr;](#tldr)
  * [Table of contents](#table-of-contents)
  * [Installation](#installation)
    * [Gradle](#gradle)
  * [Quick start](#quick-start)
    * [Producer](#producer)
    * [Consumer](#consumer)
  * [Run the example module](#run-the-example-module)
    * [Serializers](#serializers)
  * [Development](#development)
<!-- TOC -->

## Installation

### Gradle

build.gradle.kts

```kotlin
// Add jitpack
repositories {
    maven { url = uri("https://jitpack.io") }
}

// Include packages
dependencies {
    implementation("com.github.cdekok:kueue-core:0.1.0")
    implementation("com.github.cdekok:kueue-pg-vertx:0.1.0")
    implementation("com.github.cdekok:kueue-retry:0.1.0")
    implementation("com.github.cdekok:kueue-serializer-kotlinx:0.1.0")
}
```

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
/**
 * Process messages one by one
 */
class TestListener : EventListener {
    @EventHandler
    fun on(event: RecordCreated) {
        logger.info { "handle record created $event" }
    }

    @EventHandler
    fun on(event: RecordCreated) {
        logger.info { "handle record created $event" }
    }
}

/**
 * Batch process messages
 */
class BatchListener : EventListener {
    @EventHandler
    fun on(event: List<RecordCreated>) {
        logger.info { "handle record created ${event.size}" }
    }
}

val listeners = listOf(
    TestListener(),
    BatchListener(),
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

### Serializers

It's possible to run the example with a different serializer with the serializer option.

```shell
./kueue-example-pg/run producer --amount=100 --serializer=XSTREAM
./kueue-example-pg/run consumer --serializer=XSTREAM
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
./gradlew dependencyUpdates
```

Update dependencies

```shell
./gradlew versionCatalogUpdate
```
