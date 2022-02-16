package grpc.server.service;

import io.grpc.stub.StreamObserver;
import grpc.HelloRequest;
import grpc.HelloResponse;
import grpc.HelloServiceGrpc;

public class HelloServiceImpl extends HelloServiceGrpc.HelloServiceImplBase {

    @Override
    public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        System.out.println("Rceived request");
        HelloResponse response = HelloResponse.newBuilder()
                .setMessage(String.format("Hello %s, nice to meet you!", request.getName()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
