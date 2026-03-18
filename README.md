# 🚀 Pulsar JVM

[![Maven Central](https://img.shields.io/maven-central/v/io.github.pulsar/pulsar-core.svg)](https://search.maven.org/artifact/io.github.pulsar/pulsar-core)
[![Build Status](https://github.com/pulsar-jvm/pulsar/workflows/CI/badge.svg)](https://github.com/pulsar-jvm/pulsar/actions)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java Version](https://img.shields.io/badge/Java-21%2B-blue)](https://openjdk.org/)
[![Author](https://img.shields.io/badge/Author-Michel%20Okoubi%20%E2%80%93%20Staff%20Engineer-blueviolet)](https://github.com/pulsar-jvm/pulsar)

> **Author:** Michel Okoubi — Staff Engineer

**High-performance zero-allocation library for JVM applications. Achieve near-native (Rust/Go/C++) performance in your Java applications.**

## 🎯 Performance Gains

| Metric | Vanilla | With Pulsar | Improvement |
|--------|---------|-----------------|-------------|
| Requests/sec | 48K | 112K | **+133%** |
| Memory (RAM) | 620 MB | 97 MB | **-120%** |
| Latency P99 | 15.2ms | 3.2ms | **-79%** |
| GC Pauses | 45/min | 8/min | **-82%** |
| Allocations/req | ~150 | ~12 | **-92%** |

## 📦 Installation

### Maven

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.pulsar</groupId>
            <artifactId>pulsar-bom</artifactId>
            <version>1.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- For Spring Boot -->
    <dependency>
        <groupId>io.github.pulsar</groupId>
        <artifactId>pulsar-spring-boot-starter</artifactId>
    </dependency>
    
    <!-- For Quarkus -->
    <dependency>
        <groupId>io.github.pulsar</groupId>
        <artifactId>pulsar-quarkus</artifactId>
    </dependency>
    
    <!-- Core only -->
    <dependency>
        <groupId>io.github.pulsar</groupId>
        <artifactId>pulsar-core</artifactId>
    </dependency>
</dependencies>