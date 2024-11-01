package pt.tecnico.distledger.namingserver;

import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.RegisterRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.RegisterResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.DeleteRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.DeleteResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.LookupRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.LookupResponse;

import io.grpc.stub.StreamObserver;
import java.util.logging.Logger;

public class NamingServerServiceImpl extends NamingServerServiceGrpc.NamingServerServiceImplBase {

    private NamingServices namingServices;

    public NamingServerServiceImpl() {
        namingServices = new NamingServices();
    }

    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        namingServices.register(request.getName(), request.getAddress(), request.getQualifier());
        responseObserver.onNext(RegisterResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void delete(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {
        namingServices.delete(request.getName(), request.getAddress());
        responseObserver.onNext(DeleteResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void lookup(LookupRequest request, StreamObserver<LookupResponse> responseObserver) {

        namingServices.lookup(request.getName(), request.getQualifierList());
        responseObserver.onNext(LookupResponse.newBuilder().addAllAddress(namingServices.lookup(request.getName(), request.getQualifierList())).build());
        responseObserver.onCompleted();
    }

}