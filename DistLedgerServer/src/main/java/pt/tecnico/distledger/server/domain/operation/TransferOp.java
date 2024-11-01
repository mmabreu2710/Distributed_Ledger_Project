package pt.tecnico.distledger.server.domain.operation;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;import java.util.ArrayList;

public class TransferOp extends Operation {
    private String destAccount;
    private int amount;

    public TransferOp(String fromAccount, String destAccount, int amount, ArrayList<Integer> prevTS, ArrayList<Integer> TS) {
        super(fromAccount, DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO, prevTS, TS);
        this.destAccount = destAccount;
        this.amount = amount;
        setPrevTSList(prevTS);
        setTSList(TS);
        setStable(false);
    }

    public String getDestAccount() {
        return destAccount;
    }

    public void setDestAccount(String destAccount) {
        this.destAccount = destAccount;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String toString() {
        String result = "\tledger {\n\t\ttype: OP_TRANSFER_TO\n\t\tuserId: "+ getAccount() +"\n\t\tdestUserId: "+ getDestAccount() +"\n\t\tamount: "+ getAmount() +"\n\t}";
        return result;
    }
}
