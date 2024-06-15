# gRPC Example with Spring Boot and Kubernetes

This project demonstrates how to set up a gRPC server and client using Spring Boot and how to deploy them in a Kubernetes environment. The server listens on port 9090, and the client listens on port 9091.

## Prerequisites

- Java 21
- Gradle
- Protobuf Compiler (`protoc`)
- Kubernetes cluster

## Project Structure

- `grpc-server`: Contains the gRPC server application.
- `grpc-client`: Contains the gRPC client application.

## gRPC Server Setup

### 1. Create a new Spring Boot project for the server

#### `build.gradle`

```gradle
buildscript {
    ext {
        protobufVersion = '3.25.1'
        protobufPluginVersion = '0.8.14'
        grpcVersion = '1.58.1'
    }
}

plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.6'
    id 'io.spring.dependency-management' version '1.1.5'
    id 'application'
    id 'idea'
    id 'com.google.protobuf' version '0.9.4'
}

group = 'com.taeny.papa'
version = '0.0.1-SNAPSHOT'

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${protobufVersion}"
    }
    clean {
        delete generatedFilesBaseDir
    }
    plugins {
        grpc {
            artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}"
        }
    }
    generateProtoTasks {
        all()*.plugins {
            grpc{}
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    compileOnly 'org.projectlombok:lombok'
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'

//  for grpc
    implementation "com.google.protobuf:protobuf-java-util:3.25.1"
    implementation 'com.google.protobuf:protobuf-java:3.25.1'

    // https://mvnrepository.com/artifact/net.devh/grpc-server-spring-boot-starter
    implementation group: 'net.devh', name: 'grpc-server-spring-boot-starter', version: '3.1.0.RELEASE'
    // https://mvnrepository.com/artifact/net.devh/grpc-client-spring-boot-starter
    implementation group: 'net.devh', name: 'grpc-client-spring-boot-starter', version: '3.1.0.RELEASE'

    runtimeOnly "io.grpc:grpc-netty-shaded:${grpcVersion}"
    implementation "io.grpc:grpc-protobuf:${grpcVersion}"
    implementation "io.grpc:grpc-stub:${grpcVersion}"

    implementation 'com.google.protobuf:protobuf-java:3.21.12'

    compileOnly 'org.apache.tomcat:annotations-api:6.0.53'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

### 2. make grpc ServiceImpl file.

```java
package com.taeny.papa.grpc;

@GrpcService
public class HelloServiceImpl {
}
```

### 3. make proto file
```protobuf
syntax = "proto3";

option java_multiple_files = true;
option java_package = "com.taeny.papa.grpc"; // your package path
option java_outer_classname = "HelloProto";

package hello;

service HelloService {
  rpc SayHello (HelloRequest) returns (HelloReply) {}
}

message HelloRequest {
  string name = 1;
}

message HelloReply {
  string message = 1;
}
```

### 4. coding your serviceImpl
```java
package com.taeny.papa.grpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class HelloServiceImpl extends HelloServiceGrpc.HelloServiceImplBase {
    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        String name = request.getName();
        String message = """
            {
                "message": "Hello, %s"
            }
            """.formatted(name);

        HelloReply reply = HelloReply.newBuilder()
                .setMessage(message)
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}

```