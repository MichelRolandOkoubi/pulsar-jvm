# Getting Started with Pulsar JVM

> **Author:** Michel Okoubi — Staff Engineer


## Prerequisites

- Java 21+
- Maven 3.9+

## Installation

Add the BOM to your project:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.pulsar</groupId>
      <artifactId>pulsar-bom</artifactId>
      <version>1.0.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

Then add the modules you need:

```xml
<!-- Zero-allocation core -->
<dependency>
  <groupId>io.pulsar</groupId>
  <artifactId>pulsar-core</artifactId>
</dependency>

<!-- Spring Boot starter -->
<dependency>
  <groupId>io.pulsar</groupId>
  <artifactId>pulsar-spring-boot-starter</artifactId>
</dependency>
```

## Quick Example: Object Pool

```java
ZeroAllocPool<MyObject> pool = new ZeroAllocPool<>(
    PoolConfig.of(256), MyObject::new
);

MyObject obj = pool.acquire();
// use obj...
pool.release(obj);
```

## Quick Example: Zero-Copy JSON

```java
byte[] json = "{\"name\":\"pulsar\"}".getBytes(UTF_8);
ZeroCopyJsonParser parser = new ZeroCopyJsonParser(json);
parser.nextToken(); // START_OBJECT
parser.nextToken(); // FIELD_NAME "name"
```

## Spring Boot Auto-configuration

Just add `pulsar-spring-boot-starter` to your classpath —  
auto-configuration kicks in automatically.

Configure via `application.properties`:

```properties
pulsar.pool.max-size=512
pulsar.pool.preallocate=true
```
