package coen317.project.documenteditor;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Component
public class NodesInfo {
    private Map<Integer, String> nodeMap;
    private Map<Integer, Timer> timerMap = new HashMap<>();
    @Value("${self}")
    private int self;

    @Value("${leader}")
    private int leader;

    @Value("${load-balancer}")
    private String loadBalancer;

    @Value("#{${nodeMap}}")
    public void setNodeMap(Map<Integer, String> nodeMap) {
        this.nodeMap = new ConcurrentHashMap<>(nodeMap);
    }

    public synchronized void removeNode(Integer node) {
        log.info("Removing node {}", node);
        nodeMap.remove(node);
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

    public boolean isLeader() {
        return self == leader;
    }
}
