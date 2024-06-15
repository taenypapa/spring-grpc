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
plugins {
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.0.13.RELEASE'
    id 'com.google.protobuf' version '0.8.17'
    id 'java'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'net.devh:grpc-server-spring-boot-starter:2.13.1.RELEASE'
    implementation 'com.google.protobuf:protobuf-java:3.21.12'
}

protobuf {
    protoc {
        artifact = 'com.google.protobuf:protoc:3.21.12'
    }
    plugins {
        grpc {
            artifact = 'io.grpc:protoc-gen-grpc-java:1.51.0'
        }
    }
    generateProtoTasks {
        all().each { task ->
            task.plugins {
                grpc {}
            }
        }
    }
}

tasks.withType(JavaCompile) {
    options.encoding = 'UTF-8'
}

tasks.withType(Test) {
    useJUnitPlatform()
}
```

### 2. Create the `hello.proto` file

Create a `src/main/proto/hello.proto` file with the following content:

```proto
syntax = "proto3";

option java_multiple_files = true;
option java_package = "com.taeny.papa.grpc";
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

### 3. Implement the gRPC Service

Create a `HelloServiceImpl.java` file in `src/main/java/com/taeny/papa/grpc`:

```java
package com.taeny.papa.grpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class HelloServiceImpl extends HelloServiceGrpc.HelloServiceImplBase {
    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        String name = request.getName();
        String message = "Hello, " + name;

        HelloReply reply = HelloReply.newBuilder().setMessage(message).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
```

### 4. Configure the server

Create an `application.yml` file in `src/main/resources`:

```yaml
grpc:
  server:
    port: 9090
```

## gRPC Client Setup

### 1. Create a new Spring Boot project for the client

#### `build.gradle`

```gradle
plugins {
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.0.13.RELEASE'
    id 'com.google.protobuf' version '0.8.17'
    id 'java'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'net.devh:grpc-client-spring-boot-starter:2.13.1.RELEASE'
    implementation 'io.grpc:grpc-netty-shaded:1.51.0'
    implementation 'io.grpc:grpc-protobuf:1.51.0'
    implementation 'io.grpc:grpc-stub:1.51.0'
    implementation 'com.google.protobuf:protobuf-java:3.21.12'
    implementation 'io.grpc:grpc-core:1.51.0'
    implementation 'io.grpc:grpc-all:1.51.0'
}

protobuf {
    protoc {
        artifact = 'com.google.protobuf:protoc:3.21.12'
    }
    plugins {
        grpc {
            artifact = 'io.grpc:protoc-gen-grpc-java:1.51.0'
        }
    }
    generateProtoTasks {
        all().each { task ->
            task.plugins {
                grpc {}
            }
        }
    }
}

tasks.withType(JavaCompile) {
    options.encoding = 'UTF-8'
}

tasks.withType(Test) {
    useJUnitPlatform()
}
```

### 2. Copy `hello.proto` to the client project

Ensure the `hello.proto` file is placed in `src/main/proto`.

### 3. Implement the gRPC Client Service

Create a `GrpcClientService.java` file in `src/main/java/com/taeny/papa/grpc`:

```java
package com.taeny.papa.grpc;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class GrpcClientService {

    @GrpcClient("local-grpc-server")
    private HelloServiceGrpc.HelloServiceBlockingStub helloServiceStub;

    public String sayHello(String name) {
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloReply response = helloServiceStub.sayHello(request);
        return response.getMessage();
    }
}
```

### 4. Create a REST Controller

Create a `GrpcClientController.java` file in `src/main/java/com/taeny/papa/grpc`:

```java
package com.taeny.papa.grpc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GrpcClientController {

    private final GrpcClientService grpcClientService;

    public GrpcClientController(GrpcClientService grpcClientService) {
        this.grpcClientService = grpcClientService;
    }

    @GetMapping("/say-hello")
    public String sayHello(@RequestParam String name) {
        return grpcClientService.sayHello(name);
    }
}
```

### 5. Configure the client

Create an `application.yml` file in `src/main/resources`:

```yaml
grpc:
  client:
    local-grpc-server:
      address: 'static://localhost:9090'
      negotiationType: plaintext
  server:
    port: 9091

server:
  port: 8081
```

## Building and Running the Projects

### 1. Compile the Protocol Buffers and Build the Projects

Navigate to each project directory and run:

```sh
./gradlew build
```

### 2. Run the Server and Client

- **Run the Server**: Navigate to the `grpc-server` project directory and run:

  ```sh
  ./gradlew bootRun
  ```

- **Run the Client**: Navigate to the `grpc-client` project directory and run:

  ```sh
  ./gradlew bootRun
  ```

## Deploying to Kubernetes

### 1. Create Docker Images

Create Dockerfiles for both server and client projects, build the images, and push them to a container registry.

#### Dockerfile (example for server)

```Dockerfile
FROM openjdk:21
VOLUME /tmp
COPY build/libs/grpc-server-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

#### Build and Push Docker Images

```sh
docker build -t <your-registry>/grpc-server:latest .
docker push <your-registry>/grpc-server:latest

docker build -t <your-registry>/grpc-client:latest .
docker push <your-registry>/grpc-client:latest
```

### 2. Create Kubernetes Deployment and Service Files

#### grpc-server-deployment.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grpc-server
spec:
  replicas: 1
  selector:
    matchLabels:
      app: grpc-server
  template:
    metadata:
      labels:
        app: grpc-server
    spec:
      containers:
      - name: grpc-server
        image: <your-registry>/grpc-server:latest
        ports:
        - containerPort: 9090
---
apiVersion: v1
kind: Service
metadata:
  name: grpc-server
spec:
  selector:
    app: grpc-server
  ports:
    - protocol: TCP
      port: 9090
      targetPort: 9090
```

#### grpc-client-deployment.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grpc-client
spec:
  replicas: 1
  selector:
    matchLabels:
      app: grpc-client
  template:
    metadata:
      labels:
        app: grpc-client
    spec:
      containers:
      - name: grpc-client
        image: <your-registry>/grpc-client:latest
        ports:
        - containerPort: 9091
```

### 3. Deploy to Kubernetes

Apply the Kubernetes deployment and service files:

```sh
kubectl apply -f grpc-server-deployment.yaml
kubectl apply -f grpc-client-deployment
