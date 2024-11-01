package pt.tecnico.distledger.server.domain.operation;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import java.util.ArrayList;

public class CreateOp extends Operation {
    
    public CreateOp(String account, ArrayList<Integer> prevTS, ArrayList<Integer> TS) {
        super(account, DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT, prevTS, TS);
    }

    public String toString() {
        String result = "\tledger {\n\t\ttype: OP_CREATE_ACCOUNT\n\t\tuserId: "+ super.getAccount() +"\n\t}";
        return result;
    }
}
