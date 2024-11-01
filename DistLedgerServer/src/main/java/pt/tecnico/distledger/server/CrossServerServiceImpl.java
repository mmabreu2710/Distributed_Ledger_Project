package pt.tecnico.distledger.server;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CrossServerServiceImpl extends DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase  {
    private ServerState serverState;
    private static boolean debug;
    private static final Logger LOGGER = Logger.getLogger(AdminServiceImpl.class.getName());

    public CrossServerServiceImpl(ServerState serverState, boolean debugger){
        this.serverState = serverState;
        this.debug = debugger;
    }

    public List<Operation> reverseLedger(DistLedgerCommonDefinitions.LedgerState ledgerState) {
        List<Operation> ops = new ArrayList<>();
        for (DistLedgerCommonDefinitions.Operation op : ledgerState.getLedgerList()) {
            switch (op.getType()){
                case OP_CREATE_ACCOUNT:
                    ops.add(new CreateOp(op.getUserId(), transformListToArray(op.getPrevTSList()), transformListToArray(op.getTSList())));
                    break;
                case OP_TRANSFER_TO:
                    ops.add(new TransferOp(op.getUserId(), op.getDestUserId(), op.getAmount(), transformListToArray(op.getPrevTSList()), transformListToArray(op.getTSList())));
                    break;
                /*case OP_DELETE_ACCOUNT:
                    ops.add(new DeleteOp(op.getUserId()));
                    break;*/
            }
        }
        return ops;
    }
    public ArrayList<Integer> transformListToArray(List<Integer> array){
        ArrayList<Integer> newArray = new ArrayList<>(array);
        return newArray;
    }
    @Override
    public void propagateState(CrossServerDistLedger.PropagateStateRequest request, StreamObserver<CrossServerDistLedger.PropagateStateResponse> responseObserver) {
        List<Operation> opState = reverseLedger(request.getState());
        ArrayList<Integer> auxTS = transformListToArray(request.getReplicaTSList());
        boolean isServerA = serverState.isServerA();
        int intServerInv = serverState.serverToIntInv();
        int intServer = serverState.serverToInt();
        for (Operation op: opState ){
            switch (op.getOperationType()){
                case OP_CREATE_ACCOUNT:
                  if (serverState.GEPropagateRepl(op.getTS())) {
                      System.out.println("\n" +"entrou0");
                        serverState.addOpLedger(op);
                    }
                  if(!serverState.GEWrite(op.getPrevTS())){
                    System.out.println("\n" +"entrou1");
                    op.setStable(true);
                    serverState.putAccounts((op.getAccount()));
                    serverState.setTS(intServerInv, op.getTS().get(intServerInv));
                  }
                  else{
                    op.setStable(false);
                  }
                  break;
                case OP_TRANSFER_TO:
                    if (serverState.GEPropagateRepl(op.getTS())) {
                        System.out.println("\n" +"entrou0");
                        serverState.addOpLedger(op);
                    }
                    if(!serverState.GEWrite(op.getPrevTS())){
                        System.out.println("\n" +"entrou1");
                        op.setStable(true);
                        serverState.transferaux(op.getAccount(), ((TransferOp)op).getDestAccount(), ((TransferOp)op).getAmount());
                        serverState.setTS(intServerInv, op.getTS().get(intServerInv));
                    }
                    else{
                        op.setStable(false);
                    }
                    break;
                /*case OP_DELETE_ACCOUNT:
                    serverState.removeAccounts(opState.getAccount());
                    break;*/
            }
        }
        serverState.setRTS(intServerInv, auxTS.get(intServerInv));
        serverState.setTS(intServer, serverState.getRTS(intServer));
        for (Operation op : serverState.getLedger()){
            if(!serverState.GEWrite(op.getPrevTS()) && op.isStable()==false){
                switch (op.getOperationType()){
                    case OP_CREATE_ACCOUNT:
                        System.out.println("\n" +"entrou2");
                        op.setStable(true);
                        serverState.putAccounts((op.getAccount()));
                        serverState.setTS(intServerInv, op.getTS().get(intServerInv));
                        break;
                    case OP_TRANSFER_TO:
                        System.out.println("\n" +"entrou2");
                        op.setStable(true);
                        serverState.transferaux(op.getAccount(), ((TransferOp)op).getDestAccount(), ((TransferOp)op).getAmount());
                        serverState.setTS(intServerInv, op.getTS().get(intServerInv));
                        break;
                }

            }
        }
        System.out.println("\n" + serverState.getReplicaTimeStamps() + ":replica");
        System.out.println("\n" + serverState.getTimeStamps() + ":Real");
      if (debug) {
            LOGGER.info("Server -> PropagateState-> SUCCESS: propagated State");
        }
        responseObserver.onNext(CrossServerDistLedger.PropagateStateResponse.newBuilder().build());
        responseObserver.onCompleted();
  }

    public void checkIfActive(CrossServerDistLedger.CheckIfActiveRequest request, StreamObserver<CrossServerDistLedger.CheckIfActiveResponse> responseObserver){
        boolean check = serverState.isActivated();
      if (debug) {
          LOGGER.info("Server -> CheckIfActive-> SUCCESS: checked if active");
      }
      responseObserver.onNext(CrossServerDistLedger.CheckIfActiveResponse.newBuilder().setActive(check).build());
      responseObserver.onCompleted();

  }

}
