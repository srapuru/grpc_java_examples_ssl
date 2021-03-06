package grpc.server;

// git_token
// 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

import io.grpc.BindableService;
import io.grpc.Server;

import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import grpc.server.service.HelloServiceImpl;


public class App {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    private final int PORT;

    private final Server server;

    public App(int port, SslContext sslContext) {
        server = NettyServerBuilder.forPort(port)
                .sslContext(sslContext)
                .addService((BindableService) new HelloServiceImpl())
                .build();
        PORT = port;

    }

    private void start() throws IOException, InterruptedException {


            server.start();
            System.out.println("Server started, listening on " + PORT);

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("*** shutting down gRPC server since JVM is shutting down");
                    App.this.stop();
                    System.out.println("*** server shut down");
                } ) );
            server.awaitTermination();


//        final int numServers=3;
//        ExecutorService executorService = Executors.newFixedThreadPool(numServers);
//        for ( int i=0; i < numServers; i++) {
//            int port = 50000+i;
//            executorService.submit( () -> {
//                try {
//                    start(
//                }
//            })
//        }
    }


    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static SslContext loadTLSCredentials() throws SSLException {
        File serverCertFile = new File("/Users/PANKAJ_DOLLY/GRPC/java-tutorials/grpc-client-server-with-ssl/cert/server-cert.pem");
        File serverKeyFile = new File("/Users/PANKAJ_DOLLY/GRPC/java-tutorials/grpc-client-server-with-ssl/cert/server-key.pem");
        File clientCACertFile = new File("/Users/PANKAJ_DOLLY/GRPC/java-tutorials/grpc-client-server-with-ssl/cert/ca-cert.pem");

        SslContextBuilder ctxBuilder = SslContextBuilder.forServer(serverCertFile, serverKeyFile)
                .clientAuth(ClientAuth.REQUIRE)
                .trustManager(clientCACertFile);

        return GrpcSslContexts.configure(ctxBuilder).build();
    }

    private static void startServer(int port, SslContext sslContext) throws IOException, InterruptedException {

        Server server = NettyServerBuilder.forPort(port)
                .sslContext(sslContext)
                .addService((BindableService) new HelloServiceImpl())
                .build();

        server.start();
        System.out.println(" server started, listening on port: " + server.getPort());
        server.awaitTermination();

    }
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("in .....");
/* works fine
        App app = new App(8080, App.loadTLSCredentials());
        app.start();
        //app.blockUntilShutdown();
*/
        SslContext sslContext = App.loadTLSCredentials();

        final int nServers = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(nServers);
        for (int i = 0; i < nServers; i++) {
            String name = "Server_" + i;
            int port = 50000 + i;
            executorService.submit(() -> {
                try {
                    startServer(port, sslContext);
                } catch (IOException | InterruptedException e) {
                   e.printStackTrace();
                }
            });
        }

    }

}
