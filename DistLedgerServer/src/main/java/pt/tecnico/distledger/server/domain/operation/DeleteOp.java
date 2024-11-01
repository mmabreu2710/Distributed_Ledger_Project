package pt.tecnico.distledger.server.domain.operation;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;import java.util.ArrayList;

public class DeleteOp extends Operation {

    public DeleteOp(String account, ArrayList<Integer> prevTS, ArrayList<Integer> TS) {
        super(account, DistLedgerCommonDefinitions.OperationType.OP_DELETE_ACCOUNT, prevTS, TS);
    }

    public String toString() {
        String result = "\tledger {\n\t\ttype: OP_DELETE_ACCOUNT\n\t\tuserId: "+ getAccount() +"\n\t}";
        return result;
    }
    
}
