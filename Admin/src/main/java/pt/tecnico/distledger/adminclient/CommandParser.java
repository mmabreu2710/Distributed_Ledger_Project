package pt.tecnico.distledger.adminclient;

import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.adminclient.grpc.AdminService;

import java.util.ArrayList;import java.util.Scanner;

public class CommandParser {

    private static final String SPACE = " ";
    private static final String ACTIVATE = "activate";
    private static final String DEACTIVATE = "deactivate";
    private static final String GET_LEDGER_STATE = "getLedgerState";
    private static final String GOSSIP = "gossip";
    private static final String HELP = "help";
    private static final String EXIT = "exit";

    private final AdminService adminService;
    private ArrayList<Integer> timeStamps;
    public CommandParser(AdminService adminService) {

        this.adminService = adminService;
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
    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String cmd = line.split(SPACE)[0];

            switch (cmd) {
                case ACTIVATE:
                    this.activate(line);
                    break;

                case DEACTIVATE:
                    this.deactivate(line);
                    break;

                case GET_LEDGER_STATE:
                    this.dump(line);
                    break;

                case GOSSIP:
                    this.gossip(line);
                    break;

                case HELP:
                    this.printUsage();
                    break;

                case EXIT:
                    exit = true;
                    break;

                default:
                    break;
            }

        }
    }

    private void activate(String line){
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        String server = split[1];

        try{
            adminService.activate(server);
            System.out.println("OK");
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
        }
    }

    private void deactivate(String line){
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        String server = split[1];

        try{
            adminService.deactivate(server);
            System.out.println("OK");
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
        }
    }

    private void dump(String line){
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        String server = split[1];

        System.out.println("OK");
        System.out.println(adminService.getLedgerState(server));
    }

    @SuppressWarnings("unused")
    private void gossip(String line){
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        String server = split[1];

        try{
            adminService.gossip(server);
            System.out.println("OK");
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
        }
    }
    private void printUsage() {
        System.out.println("Usage:\n" +
                "- activate <server>\n" +
                "- deactivate <server>\n" +
                "- getLedgerState <server>\n" +
                "- gossip <server>\n" +
                "- exit\n");
    }
    
}
