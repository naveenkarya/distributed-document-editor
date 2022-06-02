package coen317.project.documenteditor;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
@Getter
@Component
public class NodesConfig {
    private Map<String, Deque<String>> docUserQueue = new ConcurrentHashMap<>();
    @Setter
    private Map<Integer, String> nodeMap = new ConcurrentHashMap<>();
    private Map<Integer, Timer> timerMap = new ConcurrentHashMap<>();
    @Value("${self:0}")
    private int self;

    @Value("${server.port}")
    private int selfPort;

    @Value("${leader:0}")
    @Setter
    private int leader;

    @Value("${load-balancer}")
    private String loadBalancer;

    public void setSelf(int self) {
        this.self = self;
    }

    public synchronized void removeNode(Integer node) {
        log.info("Removing node {}", node);
        nodeMap.remove(node);
    }

    public synchronized void addNode(Integer node, String address) {

        nodeMap.put(node, address);
        log.info("Add node {}. Node Map: {}", node, nodeMap);
    }

    public synchronized void removeLeader() {
        log.info("Removing leader node {} from map", leader);
        nodeMap.remove(leader);
    }

    public void setNewLeader(int leader) {
        removeLeader();
        log.info("Assigning leader {}", leader);
        this.leader = leader;
    }
    public void createPingTimers() {
        for(Integer follower : nodeMap.keySet()) {
            Timer timer = new Timer();
            RemoveFailedNode removeFailedNode = new RemoveFailedNode(follower, this);
            timerMap.put(follower, timer);
            timer.schedule(removeFailedNode, 2000);
        }
    }

    public boolean isLeader() {
        return self != 0 && self == leader;
    }

    public void addToQueue(String documentId, String user) {
        Deque<String> queue = docUserQueue.getOrDefault(documentId, new ConcurrentLinkedDeque<>());
        if(queue.contains(user)) {
            queue.remove(user);
            queue.addFirst(user);
        }
        else {
            queue.add(user);
        }
        this.docUserQueue.put(documentId, queue);
    }
    public String getNextUser(String documentId) {
        return docUserQueue.get(documentId).poll();
    }
}
