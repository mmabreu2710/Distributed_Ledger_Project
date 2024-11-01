package pt.tecnico.distledger.namingserver.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.distledger.namingserver.NamingServer;import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;import java.util.List;

public class NamingServerService {
    private final ManagedChannel channel;
    private final NamingServerServiceGrpc.NamingServerServiceBlockingStub stub;

    public NamingServerService(String host, Integer port) {
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        stub = NamingServerServiceGrpc.newBlockingStub(channel);
    }
    public ManagedChannel getChannel() {
        return channel;
    }

    public NamingServerDistLedger.RegisterResponse register(String service, String address, String qualifier) {
        NamingServerDistLedger.RegisterResponse response = stub.register(NamingServerDistLedger.RegisterRequest.newBuilder().setName(service).setAddress(address).setQualifier(qualifier).build());
        return response;
    }
    public NamingServerDistLedger.DeleteResponse delete(String service, String address) {
        NamingServerDistLedger.DeleteResponse response = stub.delete(NamingServerDistLedger.DeleteRequest.newBuilder().setName(service).setAddress(address).build());
        return response;
    }
    public NamingServerDistLedger.LookupResponse lookup(String service, List<String> qualifier) {
        NamingServerDistLedger.LookupResponse response = stub.lookup(NamingServerDistLedger.LookupRequest.newBuilder().setName(service).addAllQualifier(qualifier).build());
        return response;
    }
}
