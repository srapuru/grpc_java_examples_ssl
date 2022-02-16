package grpc.client;

import io.grpc.*;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import grpc.HelloRequest;
import grpc.HelloResponse;
import grpc.HelloServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private final HelloServiceGrpc.HelloServiceBlockingStub stub;

    public App(Channel channel) {
        stub = HelloServiceGrpc.newBlockingStub(channel);
    }

    public void hello(String name) {
        LOGGER.info("Will try to greet the server as {} ...", name);

        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloResponse response = stub.hello(request);

        LOGGER.info("Server response: {}", response.getMessage());
    }

    public static SslContext loadTLSCredentials() throws SSLException {
        File serverCACertFile = new File("/Users/PANKAJ_DOLLY/GRPC/java-tutorials/grpc-client-server-with-ssl/cert/ca-cert.pem");
        File clientCertFile = new File(  "/Users/PANKAJ_DOLLY/GRPC/java-tutorials/grpc-client-server-with-ssl/cert/client-cert.pem");
        File clientKeyFile = new File(   "/Users/PANKAJ_DOLLY/GRPC/java-tutorials/grpc-client-server-with-ssl/cert/client-key.pem");

        return GrpcSslContexts.forClient()
                .keyManager(clientCertFile, clientKeyFile)
                .trustManager(serverCACertFile)
                .build();
    }


    public static void main(String[] args) throws SSLException {
//        SSLFactory sslFactory = SSLFactory.builder()
//                .withIdentityMaterial("client/identity.jks", "secret".toCharArray())
//                .withTrustMaterial("client/truststore.jks", "secret".toCharArray())
//                .withDefaultTrustMaterial()
//                .build();

     /* works fine  - no load balanceing
        SslContext sslContext = App.loadTLSCredentials();


        ManagedChannel channel = NettyChannelBuilder.forAddress("0.0.0.0", 8080)
                .sslContext(sslContext)
                .build();

        App app = new App(channel);
        app.hello("John");

        channel.shutdown();

*/
        // For  load balancing
        NameResolver.Factory nameResolverFactory = new MultiAddressNameResolverFactory(
                new InetSocketAddress("0.0.0.0", 50000),
                new InetSocketAddress("0.0.0.0", 50001),
                new InetSocketAddress("0.0.0.0", 50002)
        );

        ManagedChannel ch = NettyChannelBuilder.forTarget("service")
                //ManagedChannelBuilder.forTarget("service")
                .nameResolverFactory(nameResolverFactory)
               .defaultLoadBalancingPolicy("round_robin")
                .sslContext(App.loadTLSCredentials())
                .build();

        HelloServiceGrpc.HelloServiceBlockingStub stub = HelloServiceGrpc.newBlockingStub(ch);
        for (int i = 0; i < 10; i++) {
            HelloRequest request = HelloRequest.newBuilder().setName("name").build();
            HelloResponse response = stub.hello(request);
            System.out.println(response.getMessage());
        }
        ch.shutdown();
    }

}


class MultiAddressNameResolverFactory extends NameResolver.Factory {

    final List<EquivalentAddressGroup> addresses;

    MultiAddressNameResolverFactory(SocketAddress... addresses) {
        this.addresses = Arrays.stream(addresses)
                .map(EquivalentAddressGroup::new)
                .collect(Collectors.toList());
    }

    public NameResolver newNameResolver(URI notUsedUri, NameResolver.Args args) {
        System.out.println("====IN newNameResolver fun "+notUsedUri + ", and args are ---"+ args);
        System.out.println("====IN  URI,"+notUsedUri.getScheme());
        return new NameResolver() {
            @Override
            public String getServiceAuthority() {
                System.out.println("====IN newNameResolver getService  ");
                return
                        "testing";
                //"0.0.0.0";
                //"fakeAuthority";
            }
            public void start(Listener2 listener) {
                System.out.println("====IN newNameResolver start  ");
                listener.onResult(ResolutionResult.newBuilder().setAddresses(addresses).setAttributes(Attributes.EMPTY).build());

            }
            public void shutdown() {
                System.out.println("====IN newNameResolver shut  ");
            }
        };
    }

    @Override
    public String getDefaultScheme() {
        return "multiaddress";
    }
}


