package pt.tecnico.distledger.namingserver;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.lang.InterruptedException;

public class NamingServer {

    public static void main(String[] args) {
        System.out.println(NamingServer.class.getSimpleName());
        System.out.printf("Received %d Argument(s)%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("args[%d] = %s%n", i, args[i]);
        }

        final BindableService impl = new NamingServerServiceImpl();

        Server server = ServerBuilder.forPort(5001).addService(impl).build();

        try {
            server.start();
        }
        catch (IOException e) {
            System.err.println("Couldn't start server!");
        }

        System.out.println("Server started");

        try {
            server.awaitTermination();
        }
        catch (InterruptedException ie){
            System.err.println("Couldn't terminate server!");
        }
    }
}
