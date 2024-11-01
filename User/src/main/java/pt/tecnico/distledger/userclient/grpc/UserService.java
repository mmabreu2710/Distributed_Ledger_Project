package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger;import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;import java.util.ArrayList;import java.util.List;

// This class is the client-side logic of grpc.
public class UserService {

    private final ManagedChannel channel;
    private UserServiceGrpc.UserServiceBlockingStub stub;
    private final NamingServerServiceGrpc.NamingServerServiceBlockingStub stubnam;

    // Constructor.
    public UserService(String host, Integer port) {
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        stubnam = NamingServerServiceGrpc.newBlockingStub(channel);
    }

    public ManagedChannel getChannel() {
        return channel;
    }
    public int getPortlookup(String server){
        List<String> serverList = new ArrayList<>();
        serverList.add(server);
        NamingServerDistLedger.LookupResponse responsenam = stubnam.lookup(NamingServerDistLedger.LookupRequest.newBuilder().setName("Distledger").addAllQualifier(serverList).build());
        List<String> allPorts = responsenam.getAddressList();
        int port = Integer.parseInt(allPorts.get(0));
        return port;
    }
    public UserServiceGrpc.UserServiceBlockingStub stubaux(int port){
        final ManagedChannel channelaux = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build();
        UserServiceGrpc.UserServiceBlockingStub stubAuxiliar = UserServiceGrpc.newBlockingStub(channelaux);
        return stubAuxiliar;
    }

    // Resquests the createAccount method and returns the response.
    public UserDistLedger.CreateAccountResponse createAccount(String userId, String server, ArrayList<Integer> timestamps) {
        int port = getPortlookup(server);
        stub = stubaux(port);
        UserDistLedger.CreateAccountResponse response = stub.createAccount(UserDistLedger.CreateAccountRequest.newBuilder().setUserId(userId).addAllPrevTS(timestamps).build());
        return response;
    }

    // Resquests the deleteAccount method and returns the response. not for fase 3
    /*public UserDistLedger.DeleteAccountResponse deleteAccount(String userId, String server) {
        int port = getPortlookup(server);
        stub = stubaux(port);
        UserDistLedger.DeleteAccountResponse response = stub.deleteAccount(UserDistLedger.DeleteAccountRequest.newBuilder().setUserId(userId).build());
        return response;
    }*/

    // Resquests the balance method and returns the response.
    public UserDistLedger.BalanceResponse balance(String userId, String server, ArrayList<Integer> timestamps) {
        int port = getPortlookup(server);
        stub = stubaux(port);
        UserDistLedger.BalanceResponse response = stub.balance(UserDistLedger.BalanceRequest.newBuilder().setUserId(userId).addAllPrevTS(timestamps).build());
        return response;
    }

    // Resquests the transferTo method and returns the response.
    public UserDistLedger.TransferToResponse transferTo(String accountFrom, String accountTo, int amount, String server, ArrayList<Integer> timestamps) {
        int port = getPortlookup(server);
        stub = stubaux(port);
        UserDistLedger.TransferToResponse response = stub.transferTo(UserDistLedger.TransferToRequest.newBuilder().setAccountFrom(accountFrom).setAccountTo(accountTo).setAmount(amount).addAllPrevTS(timestamps).build());
        return response;
    }
    
}
