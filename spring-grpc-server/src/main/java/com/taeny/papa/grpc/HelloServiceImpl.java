package com.taeny.papa.grpc;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
public class HelloServiceImpl extends HelloServiceGrpc.HelloServiceImplBase {
    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        String name = request.getName();
        String id = request.getId();

        log.info("request name: {}", name);
        log.info("request id: {}", id);

        String result = "SUCCESS";
        String types = "M,R";

        HelloReply reply = HelloReply.newBuilder()
                .setResult(result)
                .setTypes(types)
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
