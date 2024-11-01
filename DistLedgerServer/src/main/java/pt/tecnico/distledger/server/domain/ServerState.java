package pt.tecnico.distledger.server.domain;

import io.grpc.ManagedChannel;import io.grpc.ManagedChannelBuilder;import pt.tecnico.distledger.namingserver.ServerEntry;import pt.tecnico.distledger.namingserver.grpc.NamingServerService;import pt.tecnico.distledger.server.AdminServiceImpl;import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger;
import io.grpc.Status.*;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

// This class implements all the methods to access and interact with the state.
public class ServerState {
    private List<Operation> ledger;
    private HashMap<String, Integer> accounts;
    boolean activated;
    boolean serverA;
    String port;
    NamingServerService namingServerService;
    List<String> allPorts;
    private ArrayList<Integer> timeStamps;
    private ArrayList<Integer> replicaTimeStamps;

    // Constructor.
    public ServerState(String qualifier, String port, NamingServerService namingServerService) {
        if (qualifier.equals("A")){
            serverA = true;
        }
        this.port = port;
        this.ledger = new ArrayList<>();
        this.accounts = new HashMap<String,Integer>();
        accounts.put("broker", 1000);
        this.activated = true;
        this.namingServerService = namingServerService;
        timeStamps = new ArrayList<>();
        timeStamps.add(0);
        timeStamps.add(0);
        replicaTimeStamps = new ArrayList<>();
        replicaTimeStamps.add(0);
        replicaTimeStamps.add(0);
    }
    public int serverToInt(){
        if(isServerA()){
            return 0;
        }
        else{
            return 1;
        }
    }
    public int serverToIntInv(){
        if(isServerA()){
            return 1;
        }
        else{
            return 0;
        }
    }
  public ArrayList<Integer> getTimeStamps() {
    return timeStamps;
  }
  public void setTimeStamps(ArrayList<Integer> time){this.timeStamps = time;}

  public ArrayList<Integer> getReplicaTimeStamps() {
    return replicaTimeStamps;
  }

  public boolean isServerA() {
    return serverA;
  }

  public Integer getTS(Integer i) {
    return timeStamps.get(i);
    }
    public Integer getRTS(Integer i) {
        return replicaTimeStamps.get(i);
    }
    public void setRTS(Integer i, Integer value){replicaTimeStamps.set(i,value);}
    public void setTS(Integer i, Integer value){timeStamps.set(i,value);}
    public boolean GE(ArrayList<Integer> otherTimeStamp){
        for (int i = 0;i<otherTimeStamp.size();i++){
            if (otherTimeStamp.get(i) > this.timeStamps.get(i)){
                return true;
            }
        }
        return false;
    }
    public boolean GEWrite(ArrayList<Integer> otherTimeStamp){
        int index;
        if (isServerA()){
            index = 0;
        }
        else{
            index = 1;
        }
        if (otherTimeStamp.get(index) > this.timeStamps.get(index)){
            return true;
        }
        return false;
    }
    public boolean GEPropagateRepl(ArrayList<Integer> otherTimeStamp){
        int index;
        if (isServerA()){
            index = 1;
        }
        else{
            index = 0;
        }
        if (otherTimeStamp.get(index) > this.replicaTimeStamps.get(index)) {
            return true;
        }
        return false;
    }

    public boolean RGE(ArrayList<Integer> otherTimeStamp){
        for (int i = 0;i<otherTimeStamp.size();i++){
            if (otherTimeStamp.get(i) > this.replicaTimeStamps.get(i)){
                return false;
            }
        }
        return true;
    }
    public void incrementTS(ArrayList<Integer> ts){
        int index;
        if (isServerA()){
            index = 0;
        }
        else{
            index = 1;
        }
        Integer time = ts.get(index);
        time = time +1;
        ts.set(index, time);
    }

  public boolean isActivated() {
    return activated;
  }

  public synchronized void activate() throws ServerActive {
        if (activated){
            throw new ServerActive("Server already active");
        }
        else{
            this.activated = true;
        }
    }
    
    public synchronized void deactivate() throws ServerNotActive{
        if (!activated){
            throw new ServerNotActive("Server already not active");
        }
        else{
            this.activated = false;
        }
    }
    public void putAccounts(String userId){accounts.put(userId,0);}

    //public void removeAccounts(String userId){accounts.remove(userId);}
    public void transferaux(String accountFrom, String accountTo, int amount){
        accounts.put(accountFrom, accounts.get(accountFrom) - amount);
        accounts.put(accountTo, accounts.get(accountTo) + amount);
    }

    public List<String> lookupAllPorts(){
        NamingServerDistLedger.LookupResponse response = namingServerService.lookup("Distledger",new ArrayList<>());
        List<String> allPorts = new ArrayList<>(response.getAddressList());
        allPorts.remove(this.port);
        return allPorts;
    }
    // Creates an account with a certain user.
    public synchronized void createAccount(String userId, ArrayList<Integer> prev) throws AccountAlreadyCreated, ServerNotActive{
        if (accounts.containsKey(userId)){
            throw new AccountAlreadyCreated("Account " + userId + " already created");
        }
        else if (!this.activated){
            throw new ServerNotActive("Server not active");
        }
        else{
            incrementTS(replicaTimeStamps);
            System.out.println("\n" + this.getReplicaTimeStamps() + ":replica");
            if (GE(prev)){
                CreateOp op = new CreateOp(userId, prev, replicaTimeStamps);
                op.setStable(false);
                ledger.add(op);
                System.out.println("\n" + "operation not done, because of timestamps");
            }
            else{
                CreateOp op = new CreateOp(userId, prev, replicaTimeStamps);
                op.setStable(true);
                ledger.add(op);
                accounts.put(userId, 0);
                timeStamps = replicaTimeStamps;
                System.out.println("\n" + this.timeStamps.toString());
            }
    }
  }

  // Deletes an account of a certain user. not needed for fase 3
  /*public synchronized void deleteAccount(String userId) throws NonExistentAccount, ServerNotActive, BrokerCantBeDeleted, CantDeleteBalancePositive, OtherServerNotAvailable{
      if (!accounts.containsKey(userId)){
          throw new NonExistentAccount("Account " + userId + " not created");
      }
      else if (!this.activated){
          throw new ServerNotActive("Server not active");
      }
      else if (userId.equals("broker")){
          throw new BrokerCantBeDeleted("Broker cant be deleted");
      }
      else if (accounts.get(userId) > 0){
          throw new CantDeleteBalancePositive("Balance greater than 0");
      }
      else if(!checkIfActive()){
          throw new OtherServerNotAvailable("Other Server not available");
      }

      else{
          accounts.remove(userId);
          ledger.add(new DeleteOp(userId));
          List<Operation> ledgeraux = new ArrayList<>();
          ledgeraux.add(new DeleteOp(userId));
          propagateState(ledgeraux);
      }
  }*/

  // Joins the toString's of all the operations on the ledger to create a print message.
  public synchronized String getLedgerState() {
        String result = "ledgerState {\n";
        for(Operation op: ledger){
            result += op.toString() + "\n";
        }

        result += "}";
        return result;
    }

    public synchronized List<Operation> getLedger() {
        return ledger;
    }
    public void addOpLedger(Operation op){
        ledger.add(op);
    }

    // Checks the balance of a certain user.
    public synchronized int balance(String userId, ArrayList<Integer> prev) throws NonExistentAccount, ServerNotActive, TimeStampError{
        if (GE(prev)){
            throw new TimeStampError("TimeStamp Error: timeStamp of User Greater than of server");
        }
        else if (!accounts.containsKey(userId) && !GE(prev)){
            throw new NonExistentAccount("Account " + userId + " not created");
        }
        else if (!this.activated){
            throw new ServerNotActive("Server not active");
        }
        else{
            return accounts.get(userId);     
        }
    }

    // Transfers an amount from a user to another.
    public synchronized void transferTo(String accountFrom, String accountTo, int amount, ArrayList<Integer> prev) throws NonExistentAccount, ServerNotActive, BalanceNotEnough, NegativeOrNullAmmount{
        if (!accounts.containsKey(accountFrom) && !GE(prev)){
            throw new NonExistentAccount("Account " + accountFrom + " not created");
        }
        else if (!accounts.containsKey(accountTo) && !GE(prev)){
            throw new NonExistentAccount("Account " + accountTo + " not created");
        }
        else if (!this.activated){
            throw new ServerNotActive("Server not active");
        }
        else if (accounts.get(accountFrom) < amount){
            throw new BalanceNotEnough("Balance Not enough");
        }
        else if (amount <= 0){
            throw new NegativeOrNullAmmount("Amount not accepted");
        }
        else{
            replicaTimeStamps = timeStamps;
            incrementTS(replicaTimeStamps);
            if (GE(prev)){
                TransferOp op = new TransferOp(accountFrom, accountTo, amount,prev, timeStamps);
                op.setStable(false);
                ledger.add(op);
                System.out.println("\n" + "operation not done, because of timestamps");
            }
            else{
                TransferOp op = new TransferOp(accountFrom, accountTo, amount,prev, timeStamps);
                op.setStable(true);
                ledger.add(op);
                accounts.put(accountFrom, accounts.get(accountFrom) - amount);
                accounts.put(accountTo, accounts.get(accountTo) + amount);
                timeStamps = replicaTimeStamps;
                System.out.println("\n" + this.timeStamps.toString());
            }
        }
    }

  public synchronized void propagateState(List<Operation> ledger) {
      allPorts = lookupAllPorts();
      for (String portfor : allPorts) {
          ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", Integer.parseInt(portfor)).usePlaintext().build();
          DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stubfor = DistLedgerCrossServerServiceGrpc.newBlockingStub(channel);
          stubfor.propagateState(CrossServerDistLedger.PropagateStateRequest.newBuilder().setState(parseLedger(ledger)).addAllReplicaTS(replicaTimeStamps).build());
      }

  }
  public synchronized boolean checkIfActive(){
      ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 5001).usePlaintext().build();
      NamingServerServiceGrpc.NamingServerServiceBlockingStub stub = NamingServerServiceGrpc.newBlockingStub(channel);
      allPorts = lookupAllPorts();
      channel = ManagedChannelBuilder.forAddress("localhost", Integer.parseInt(allPorts.get(0))).usePlaintext().build();
      DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stubfor = DistLedgerCrossServerServiceGrpc.newBlockingStub(channel);
      CrossServerDistLedger.CheckIfActiveResponse response = stubfor.checkIfActive(CrossServerDistLedger.CheckIfActiveRequest.newBuilder().build());
      return response.getActive();
  }
  //public synchronized void checkIf

    // Exceptions
    public class AccountAlreadyCreated extends RuntimeException {
        public AccountAlreadyCreated(String errorMessage) {
            super(errorMessage);
        }
    }

    public class ServerNotActive extends RuntimeException {
        public ServerNotActive(String errorMessage) {
            super(errorMessage);
        }
    }

    public class NonExistentAccount extends RuntimeException {
        public NonExistentAccount(String errorMessage) {
            super(errorMessage);
        }
    }

    /*public class BrokerCantBeDeleted extends RuntimeException { not needed for fase 3
        public BrokerCantBeDeleted(String errorMessage) {
            super(errorMessage);
        }
    }

    public class CantDeleteBalancePositive extends RuntimeException {
        public CantDeleteBalancePositive(String errorMessage) {
            super(errorMessage);
        }
    }*/

    public class BalanceNotEnough extends RuntimeException {
        public BalanceNotEnough(String errorMessage) {
            super(errorMessage);
        }
    }

    public class NegativeOrNullAmmount extends RuntimeException {
        public NegativeOrNullAmmount(String errorMessage) {
            super(errorMessage);
        }
    }

    public class ServerActive extends RuntimeException {
        public ServerActive(String errorMessage) {
            super(errorMessage);
        }
    }
    public class OtherServerNotAvailable extends RuntimeException {
        public OtherServerNotAvailable(String errorMessage) {
            super(errorMessage);
        }
    }
    public class TimeStampError extends RuntimeException {
        public TimeStampError(String errorMessage) {
            super(errorMessage);
        }
    }
    public DistLedgerCommonDefinitions.LedgerState parseLedger(List<Operation> ops) {
        List<DistLedgerCommonDefinitions.Operation> newops = new ArrayList<>();
        DistLedgerCommonDefinitions.Operation op;
        for (Operation operation : ops){
            switch (operation.getOperationType()){
                case OP_CREATE_ACCOUNT:
                  op = DistLedgerCommonDefinitions.Operation.newBuilder()
                          .setType(operation.getOperationType())
                          .setUserId(operation.getAccount())
                          .addAllPrevTS(operation.getPrevTS())
                          .addAllTS(operation.getTS())
                          .setStable(operation.isStable())
                          .build();
                    newops.add(op);
                    break;
                case OP_TRANSFER_TO:
                  op =
                      DistLedgerCommonDefinitions.Operation.newBuilder()
                          .setType(operation.getOperationType())
                          .setUserId(operation.getAccount())
                          .setDestUserId(((TransferOp) operation).getDestAccount())
                          .setAmount(((TransferOp) operation).getAmount())
                          .addAllPrevTS(operation.getPrevTS())
                          .addAllTS(operation.getTS())
                          .setStable(operation.isStable())
                          .build();
                    newops.add(op);
                    break;
                /*case OP_DELETE_ACCOUNT:
                    op= DistLedgerCommonDefinitions.Operation.newBuilder().setType(operation.getOperationType()).setUserId(operation.getAccount()).build();
                    newops.add(op);
                    break; not needed for fase 3*/
            }
        }
        DistLedgerCommonDefinitions.LedgerState ledgerState = DistLedgerCommonDefinitions.LedgerState.newBuilder().addAllLedger(newops).build();
        return ledgerState;
    }
    
}