package pt.tecnico.distledger.server;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import static io.grpc.Status.INVALID_ARGUMENT;

import java.util.ArrayList;import java.util.List;import java.util.logging.Logger;

// This class uses grpc to create responses for the users' methods.
public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {
    private ServerState serverState;
    private static boolean debug;
    private static final Logger LOGGER = Logger.getLogger(AdminServiceImpl.class.getName());

    // Constructor.
    public UserServiceImpl(ServerState serverState, boolean debugger) {
        this.serverState = serverState;
        this.debug = debugger;
    }
    public ArrayList<Integer> transformListToArray(List<Integer> array){
        ArrayList<Integer> newArray = new ArrayList<>(array);
        return newArray;
    }

    // Creates a response for createAccount.
    @Override
    public void createAccount(UserDistLedger.CreateAccountRequest request, StreamObserver<UserDistLedger.CreateAccountResponse> responseObserver) {
        try{
            ArrayList<Integer> prev = transformListToArray(request.getPrevTSList());
            serverState.createAccount(request.getUserId(), prev);
            prev.set(serverState.serverToInt(), serverState.getRTS(serverState.serverToInt()));
            if (prev.get(serverState.serverToIntInv()) < serverState.getRTS(serverState.serverToIntInv())){
                prev.set(serverState.serverToIntInv(), serverState.getRTS(serverState.serverToIntInv()));
            }
            if (debug) {
                    LOGGER.info("User -> Create Account-> SUCCESS: account created successfully");
                }
            responseObserver.onNext(UserDistLedger.CreateAccountResponse.newBuilder().addAllTS(prev).build());
            responseObserver.onCompleted();
        } catch (ServerState.AccountAlreadyCreated e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("User already created").asRuntimeException());
        } catch (ServerState.ServerNotActive e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Server not active").asRuntimeException());
        }catch (ServerState.TimeStampError e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("TimeStamp Error: timeStamp of User Greater than of server").asRuntimeException());
        }

    }

    // Creates a response for deleteAccount. //not for fase 3
    /*@Override
    public void deleteAccount(UserDistLedger.DeleteAccountRequest request, StreamObserver<UserDistLedger.DeleteAccountResponse> responseObserver) {
        try{
            serverState.deleteAccount(request.getUserId());
            if (debug) {
                LOGGER.info("User -> Delete Account-> SUCCESS: account delete successfully");
            }

            responseObserver.onNext(UserDistLedger.DeleteAccountResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (ServerState.NonExistentAccount e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Account not created").asRuntimeException());
        } catch (ServerState.ServerNotActive e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Server not active").asRuntimeException());
        } catch (ServerState.CantDeleteBalancePositive e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Cant delete account with balance positive").asRuntimeException());
        } catch (ServerState.BrokerCantBeDeleted e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Cant delete account broker").asRuntimeException());
        }catch (ServerState.SecondaryServer e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Server not primary. Doesn't have this privilege").asRuntimeException());
        }catch (ServerState.SecondaryServerNotAvailable e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Secundary server not available").asRuntimeException());
        }
    }*/

    // Creates a response for balance.
    @Override
    public void balance(UserDistLedger.BalanceRequest request, StreamObserver<UserDistLedger.BalanceResponse> responseObserver) {
        try{
            int value = serverState.balance(request.getUserId(), transformListToArray(request.getPrevTSList()));
            if (debug) {
                LOGGER.info("User -> Show balance-> SUCCESS: account showed");
            }

            responseObserver.onNext(UserDistLedger.BalanceResponse.newBuilder().setValue(value).addAllValueTS(serverState.getTimeStamps()).build());
            responseObserver.onCompleted();
        } catch (ServerState.TimeStampError e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("TimeStamp Error: timeStamp of User Greater than of server: \n" + request.getPrevTSList().toString() + "\n" + serverState.getTimeStamps()).asRuntimeException());
        } catch (ServerState.NonExistentAccount e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Account not created").asRuntimeException());
        } catch (ServerState.ServerNotActive e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Server not active").asRuntimeException());
        }
    }

    // Creates a response for transferTo.
    public void transferTo(UserDistLedger.TransferToRequest request, StreamObserver<UserDistLedger.TransferToResponse> responseObserver) {
        try{
            serverState.transferTo(request.getAccountFrom(), request.getAccountTo(), request.getAmount(), transformListToArray(request.getPrevTSList()));
            if (debug) {
                LOGGER.info("User -> Transfer to other account-> SUCCESS: transfered balance to other account");
            }

            responseObserver.onNext(UserDistLedger.TransferToResponse.newBuilder().addAllTS(serverState.getTimeStamps()).build());
            responseObserver.onCompleted();
        } catch (ServerState.NonExistentAccount e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("One of accounts mentioned not created").asRuntimeException());
        } catch (ServerState.ServerNotActive e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Server not active").asRuntimeException());
        } catch (ServerState.BalanceNotEnough e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Balance not enough to do transfer").asRuntimeException());
        } catch (ServerState.NegativeOrNullAmmount e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Amount to be sent cant be negative or equal to 0").asRuntimeException());
        }catch (ServerState.TimeStampError e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("TimeStamp Error: timeStamp of User Greater than of server").asRuntimeException());
        }
    }
    
}
