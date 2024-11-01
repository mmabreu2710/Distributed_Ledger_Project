package pt.tecnico.distledger.namingserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NamingServices {
    
    HashMap<String, ServiceEntry> services;
    
    public NamingServices() {
        services = new HashMap<String, ServiceEntry>();
    }

    public HashMap<String, ServiceEntry> getServices() {
        return services;
    }

    public void setServices(HashMap<String, ServiceEntry> services) {
        this.services = services;
    }

    public void register(String service, String address, String qualifier) {
        ServerEntry entry = new ServerEntry(address, qualifier);
        
        if (services.containsKey(service)) {
            services.get(service).addServerEntry(entry);
        }
        else {
            ServiceEntry serviceEntry = new ServiceEntry(service);
            serviceEntry.addServerEntry(entry);
            services.put(service, serviceEntry);
        }

    }

    public void delete(String service, String address) {
        if (services.containsKey(service)) {
            ServiceEntry serviceEntry = services.get(service);
            for (ServerEntry entry : serviceEntry.getEntries()) {
                if (entry.getHostPort().equals(address)) {
                    services.get(service).entries.remove(address);
                }
            }

        }
        return;
    }

    public List<String> lookup(String service, List<String> qualifier) {
        if (services.containsKey(service)) {
            List<String> result = new ArrayList<>();
            if (qualifier.size() == 0 || qualifier.size() > 1) {
                for (ServerEntry e : services.get(service).entries) {
                    result.add(e.getHostPort());
                }
                return result;
            }
            else if (qualifier.size() == 1) {
                if (qualifier.get(0).equals("A")) {
                    for (ServerEntry e : services.get(service).entries) {
                        if (e.qualifier.equals("A")) {
                            result.add(e.getHostPort());
                        }
                    }
                }
                else if (qualifier.get(0).equals("B")) {
                    for (ServerEntry e : services.get(service).entries) {
                        if (e.qualifier.equals("B")) {
                            result.add(e.getHostPort());
                        }
                    }
                }
                return result;
            }
        }
        return new ArrayList<String>();
    }

}
