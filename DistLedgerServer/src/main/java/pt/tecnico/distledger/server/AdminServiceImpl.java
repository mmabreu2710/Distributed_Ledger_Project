package pt.tecnico.distledger.server;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;

import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static io.grpc.Status.INVALID_ARGUMENT;

// This class uses grpc to create responses for the methods of the Admin user.
public class AdminServiceImpl extends AdminServiceGrpc.AdminServiceImplBase {

    private ServerState serverState;
    private static boolean debug;
    private static final Logger LOGGER = Logger.getLogger(AdminServiceImpl.class.getName());

    // Constructor.
    public AdminServiceImpl(ServerState serverState, boolean debugger) {
         this.serverState = serverState;
         this.debug = debugger;
    }

    // Activates a server.
    @Override
    public void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {
        try{
            serverState.activate();
            if (debug) {
                LOGGER.info("Admin -> Activate -> SUCCESS: Server activated successfully.");
            }

            responseObserver.onNext(ActivateResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (ServerState.ServerActive e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Server already active").asRuntimeException());
        }
    }

    // Deactivates a server.
    @Override
    public void deactivate(DeactivateRequest request, StreamObserver<DeactivateResponse> responseObserver) {
        try{
            serverState.deactivate();
            if (debug) {
                LOGGER.info("Admin -> Deactivate -> SUCCESS: Server deactivated successfully.");
            }

            responseObserver.onNext(DeactivateResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (ServerState.ServerNotActive e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Server already not active").asRuntimeException());
        }
    }

    // Parses the ledger received from ServerState to descriminate each operation type.
    public DistLedgerCommonDefinitions.LedgerState parseLedger(List<Operation> ops) {
        List<DistLedgerCommonDefinitions.Operation> newops = new ArrayList<>();
        DistLedgerCommonDefinitions.Operation op;
        for (Operation operation : ops){
            switch (operation.getOperationType()){
                case OP_CREATE_ACCOUNT:
                    op= DistLedgerCommonDefinitions.Operation.newBuilder().setType(operation.getOperationType()).setUserId(operation.getAccount()).build();
                    newops.add(op);
                    break;
                case OP_TRANSFER_TO:
                    op= DistLedgerCommonDefinitions.Operation.newBuilder().setType(operation.getOperationType()).setUserId(operation.getAccount()).setDestUserId(((TransferOp)operation).getDestAccount()).setAmount(((TransferOp)operation).getAmount()).build();
                    newops.add(op);
                    break;
                /*case OP_DELETE_ACCOUNT:
                    op= DistLedgerCommonDefinitions.Operation.newBuilder().setType(operation.getOperationType()).setUserId(operation.getAccount()).build();
                    newops.add(op);
                    break; not needed for fase 3 */
            }
        }
        DistLedgerCommonDefinitions.LedgerState ledgerState = DistLedgerCommonDefinitions.LedgerState.newBuilder().addAllLedger(newops).build();
        return ledgerState;
    }

    // Gets the ledger from ServerState and transforms it into a LedgerState.
    public void getLedgerState(getLedgerStateRequest request, StreamObserver<getLedgerStateResponse> responseObserver) {
        serverState.getLedgerState();
        if (debug) {
            LOGGER.info("Admin -> getLedgerState -> Print Ledger");
        }
        responseObserver.onNext(getLedgerStateResponse.newBuilder().setLedgerState(parseLedger(serverState.getLedger())).build());
        responseObserver.onCompleted();
    }
    @Override
    public void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {
        try{
            serverState.propagateState(serverState.getLedger());
            if (debug) {
                LOGGER.info("Admin -> gossip -> SUCCESS: Server did gossip operation successfully.");
            }

            responseObserver.onNext(GossipResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (ServerState.ServerNotActive e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Server already not active").asRuntimeException());
        }
    }
    
}
