package pt.tecnico.distledger.namingserver;

public class ServerEntry {

    private String hostPort;
    String qualifier;

    public ServerEntry(String hostPort, String qualifier){
        setHostPort(hostPort);
        setQualifier(qualifier);
    }

    public String getHostPort() {
        return hostPort;
    }

    public void setHostPort(String hostPort) {
        this.hostPort = hostPort;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

}