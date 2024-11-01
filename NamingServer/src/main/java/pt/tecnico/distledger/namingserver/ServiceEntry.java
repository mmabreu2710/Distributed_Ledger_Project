package pt.tecnico.distledger.namingserver;

import java.util.ArrayList;
import java.util.List;

public class ServiceEntry {

    private String serviceName;
    List<ServerEntry> entries;

    public ServiceEntry(String name) {
        setServiceName(name);
        entries = new ArrayList<ServerEntry>();
    }

    public void addServerEntry(ServerEntry entry) {
        entries.add(entry);
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<ServerEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<ServerEntry> entries) {
        this.entries = entries;
    }
}
