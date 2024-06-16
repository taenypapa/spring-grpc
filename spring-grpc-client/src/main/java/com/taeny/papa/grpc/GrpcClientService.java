package com.taeny.papa.grpc;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GrpcClientService {

    @GrpcClient("local-grpc-server")
    private HelloServiceGrpc.HelloServiceBlockingStub helloServiceBlockingStub;

    public String sayHello(String name) {
        HelloRequest request = HelloRequest.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setName(name).build();
        HelloReply response = helloServiceBlockingStub.sayHello(request);
        return response.getResult();
    }
}
