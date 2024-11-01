package pt.tecnico.distledger.server;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.distledger.namingserver.grpc.NamingServerService;
import pt.tecnico.distledger.server.domain.ServerState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;

import java.io.IOException;

/*This class is the main class of the Server,
which starts the server with a received port.
*/
public class ServerMain {
    public static boolean debug = false;
    private static ServerState serverState;
    private static NamingServerService namingServerService;

    public static void main(String[] args) throws IOException, InterruptedException{

        System.out.println(ServerMain.class.getSimpleName());

        // receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // check arguments
        if (args.length != 2) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s port%n", ServerMain.class.getName());
            return;
        }
        if(args.length == 2) {
            debug = true;
        }

        final int port = Integer.parseInt(args[0]);
        final String qualifier = args[1];
        namingServerService = new NamingServerService("localhost", 5001);
        serverState = new ServerState(qualifier, args[0], namingServerService);

        // These services are used to create the responses for remote access.
        final BindableService implA = new AdminServiceImpl(serverState, debug);
        final BindableService implU = new UserServiceImpl(serverState, debug);
        final BindableService implC = new CrossServerServiceImpl(serverState, debug);

        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(port).addService(implA).addService(implU).addService(implC).build();

        // Start the server
        try {
            server.start();
            namingServerService.register("Distledger", args[0], qualifier);
        }
        catch (IOException e) {
            System.err.println("Couldn't start server!");
            System.out.println(e.toString());
        }

        System.out.println("Server started");

        System.out.println("Press enter to shutdown");
        System.in.read();
        namingServerService.delete("Distledger",args[0]);
        server.shutdown();

    }

}

