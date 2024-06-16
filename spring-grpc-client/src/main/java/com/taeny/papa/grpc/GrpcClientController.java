package com.taeny.papa.grpc;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GrpcClientController {
    private final GrpcClientService grpcClientService;

    @GetMapping("/say-hello")
    public ResponseEntity<?> sayHello(@RequestParam String name) {
        String result = grpcClientService.sayHello(name);
        return result.equals("SUCCESS") ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }
}
