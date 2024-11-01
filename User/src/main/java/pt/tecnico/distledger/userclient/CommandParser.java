package pt.tecnico.distledger.userclient;

import pt.tecnico.distledger.userclient.grpc.UserService;
import io.grpc.StatusRuntimeException;import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger;

import java.util.ArrayList;import java.util.List;import java.util.Scanner;

public class CommandParser {

    private static final String SPACE = " ";
    private static final String CREATE_ACCOUNT = "createAccount";
    private static final String DELETE_ACCOUNT = "deleteAccount";
    private static final String TRANSFER_TO = "transferTo";
    private static final String BALANCE = "balance";
    private static final String HELP = "help";
    private static final String EXIT = "exit";

    private final UserService userService;
    private ArrayList<Integer> timeStamps;

    public CommandParser(UserService userService) {

        this.userService = userService;
        timeStamps = new ArrayList<>();
        timeStamps.add(0);
        timeStamps.add(0);

    }

  public ArrayList<Integer> getTimeStamps() {
    return timeStamps;
  }

  public Integer getTS(Integer i) {
    return timeStamps.get(i);}
    public void setTS(Integer i, Integer value){timeStamps.set(i,value);}
    public ArrayList<Integer> transformListToArray(List<Integer> array){
        ArrayList<Integer> newArray = new ArrayList<>(array);
        return newArray;
    }

    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String cmd = line.split(SPACE)[0];

            try{
                switch (cmd) {
                    case CREATE_ACCOUNT:
                        this.createAccount(line);
                        break;

                    /*case DELETE_ACCOUNT: nao fazer para fase 3
                        this.deleteAccount(line);
                        break;*/

                    case TRANSFER_TO:
                        this.transferTo(line);
                        break;

                    case BALANCE:
                        this.balance(line);
                        break;

                    case HELP:
                        this.printUsage();
                        break;

                    case EXIT:
                        exit = true;
                        userService.getChannel().shutdown();
                        break;

                    default:
                        break;
                }
            }
            catch (Exception e){
                System.err.println(e.getMessage());
            }
        }
    }

    private void createAccount(String line){
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }

        String server = split[1];
        String username = split[2];
        try{
            UserDistLedger.CreateAccountResponse response = userService.createAccount(username, server, timeStamps);
            this.timeStamps = transformListToArray(response.getTSList());
            System.out.println("OK");
            System.out.println("" + this.timeStamps.toString());
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
        }
    }

    /*private void deleteAccount(String line){ //nao fazer para fase 3
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }
        String server = split[1];
        String username = split[2];
        try{
            userService.deleteAccount(username, server);
            System.out.println("OK");
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
        }
    }*/


    private void balance(String line){
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }
        String server = split[1];
        String username = split[2];
        try{
            System.out.println("OK");
            UserDistLedger.BalanceResponse response = userService.balance(username, server, timeStamps);
            this.timeStamps = transformListToArray(response.getValueTSList());
            System.out.println(response.getValue());
            System.out.println("" + this.timeStamps.toString());
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
        }
    }

    private void transferTo(String line){
        String[] split = line.split(SPACE);

        if (split.length != 5){
            this.printUsage();
            return;
        }
        String server = split[1];
        String from = split[2];
        String dest = split[3];
        Integer amount = Integer.valueOf(split[4]);
        try{
            UserDistLedger.TransferToResponse response = userService.transferTo(from, dest, amount, server, timeStamps);
            this.timeStamps = transformListToArray(response.getTSList());
            System.out.println("" + this.timeStamps.toString());
            System.out.println("OK");
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
        }
    }

    private void printUsage() {
        System.out.println("Usage:\n" +
                "- createAccount <server> <username>\n" +
                "- deleteAccount <server> <username>\n" +
                "- balance <server> <username>\n" +
                "- transferTo <server> <username_from> <username_to> <amount>\n" +
                "- exit\n");
    }
}

