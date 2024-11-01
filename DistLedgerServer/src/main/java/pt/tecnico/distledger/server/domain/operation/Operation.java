package pt.tecnico.distledger.server.domain.operation;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;import java.util.ArrayList;

public class Operation {
    private String account;
    DistLedgerCommonDefinitions.OperationType operationType;
    private ArrayList<Integer> prevTS;
    private ArrayList<Integer> TS;
    private boolean stable;



    public Operation(String fromAccount, DistLedgerCommonDefinitions.OperationType operationType, ArrayList<Integer> prevTS, ArrayList<Integer> TS) {
        this.account = fromAccount;
        this.operationType = operationType; // added operationType to descriminate each operation stored on the ledger.
        this.prevTS = prevTS;
        this.TS = TS;
        this.stable = false;
    }

    public String getAccount() {
        return account;
    }

  public ArrayList<Integer> getPrevTS() {
    return prevTS;
  }

  public ArrayList<Integer> getTS() {
    return TS;
  }

  public boolean isStable() {
    return stable;
  }

  public void setStable(boolean stable) {
    this.stable = stable;
  }

  public void setPrevTS(Integer i, Integer value){prevTS.set(i,value);}
    public void setTS(Integer i, Integer value){TS.set(i,value);}
    public void setTSList(ArrayList<Integer> arrayList){this.TS = arrayList;}
    public void setPrevTSList(ArrayList<Integer> arrayList){this.prevTS = arrayList;}

  public void setAccount(String account) {
        this.account = account;
    }

    public DistLedgerCommonDefinitions.OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(DistLedgerCommonDefinitions.OperationType operationType) {
        this.operationType = operationType;
    }
    
}
