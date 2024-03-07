package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractPoolComponent implements AutoCloseable {

    private static final Log log = LogFactory.getLog(AbstractPoolComponent.class);

    private Set<Long> clients = ConcurrentHashMap.newKeySet();

    private Map<Long, String> clientIds = new ConcurrentHashMap<>();

    private String uri;

    public void addClient() {
        final long id = Thread.currentThread().getId();
        String clientName = UUID.randomUUID().toString();
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            log.debug(ste + "\n");
        }
        clients.add(id);
        String oldName = clientIds.put(id, clientName);
        if (oldName == null) {
            log.debug(String.format("Client added to %s , client id %s, client name %s", uri, id, clientName));
        } else {
            log.debug(String.format("Client readded to %s, client id %s. Old client name %s is replaced by %s", uri, id,
                    oldName, clientName));
        }
    }

    public void removeClient() {
        final long id = Thread.currentThread().getId();
        String clientName = clientIds.remove(id);
        log.debug(String.format("Client removed from %s , client id %s, client name %s", uri, id, clientName));
        clients.remove(id);
    }

    public void removeDeadClients() {
        Set<Long> currentClients = new HashSet<Long>();
        currentClients.addAll(clients);
        Map<Long, Boolean> currentThreadIds = Thread.getAllStackTraces().keySet().stream().collect(Collectors.toMap(
                Thread::getId, Thread::isAlive));
        for (Long client : currentClients) {
            if (!currentThreadIds.containsKey(client) || currentThreadIds.get(client) == false) {
                log.error("Removed dead client thread with id " + client);
                clients.remove(client);
            }
        }
    }

    public List<String> getClients() {
        return clientIds.entrySet().stream().map(e -> String.valueOf(e.getKey()) + ":" + e.getValue()).collect(
                Collectors.toList());
    }

    public boolean hasClients() {
        return !clients.isEmpty();
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public void close() {
        removeClient();
    }

}
