package pt.tecnico.distledger.adminclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import java.util.ArrayList;import java.util.List;

// This class is the client-side logic of grpc.
public class AdminService {

    private final ManagedChannel channel;
    private AdminServiceGrpc.AdminServiceBlockingStub stub;
    private final NamingServerServiceGrpc.NamingServerServiceBlockingStub stubnam;

    // Constructor.
    public AdminService(String host, Integer port) {
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        stubnam = NamingServerServiceGrpc.newBlockingStub(channel);
    }

    public int getPortlookup(String server){
        List<String> serverList = new ArrayList<>();
        serverList.add(server);
        NamingServerDistLedger.LookupResponse responsenam = stubnam.lookup(NamingServerDistLedger.LookupRequest.newBuilder().setName("Distledger").addAllQualifier(serverList).build());
        List<String> allPorts = responsenam.getAddressList();
        int port = Integer.parseInt(allPorts.get(0));
        return port;
    }
    public AdminServiceGrpc.AdminServiceBlockingStub stubaux(int port){
        final ManagedChannel channelaux = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build();
        AdminServiceGrpc.AdminServiceBlockingStub stubAuxiliar = AdminServiceGrpc.newBlockingStub(channelaux);
        return stubAuxiliar;
    }
    // Resquests the activate method and returns the response.
    public AdminDistLedger.ActivateResponse activate(String server) {
        int port = getPortlookup(server);
        stub = stubaux(port);
        AdminDistLedger.ActivateResponse response = stub.activate(AdminDistLedger.ActivateRequest.newBuilder().build());
        return response;
    }

    // Requests the deactivate method and returns the response.
    public AdminDistLedger.DeactivateResponse deactivate(String server) {
        int port = getPortlookup(server);
        stub = stubaux(port);
        AdminDistLedger.DeactivateResponse response = stub.deactivate(AdminDistLedger.DeactivateRequest.newBuilder().build());
        return response;
    }

    // Requests the getLedgerState method and returns the response.
    public AdminDistLedger.getLedgerStateResponse getLedgerState(String server) {
        int port = getPortlookup(server);
        stub = stubaux(port);
        AdminDistLedger.getLedgerStateResponse response = stub.getLedgerState(AdminDistLedger.getLedgerStateRequest.newBuilder().build());
        return response;
    }
    //Requests the gossip method and returns the response.
    public AdminDistLedger.GossipResponse gossip(String server) {
        int port = getPortlookup(server);
        stub = stubaux(port);
        AdminDistLedger.GossipResponse response = stub.gossip(AdminDistLedger.GossipRequest.newBuilder().build());
        return response;
    }
    
}