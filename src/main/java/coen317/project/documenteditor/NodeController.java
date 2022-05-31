package coen317.project.documenteditor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Timer;

@RestController
@Slf4j
public class NodeController {
    public static final String UPDATE_QUEUE_PATH = "/updateQueue/{documentId}/{user}/{fromNode}";
    @Autowired
    NodesConfig nodesConfig;
    public static final String PING_PATH = "/ping/{number}";
    public static final String ELECTION_PATH = "/elect/{fromNode}";
    public static final String ELECTED_PATH = "/elected/{newLeader}";
    public static final String REMOVE_NODE = "/remove/{node}";

    public static final String ADD_NODE = "/add/{node}/{port}";

    @Autowired
    NodeAddService nodeAddService;

    @GetMapping(PING_PATH)
    public ResponseEntity<Void> ping(@PathVariable int number) {
        //log.info("Ping from {}", number);
        Timer timer = nodesConfig.getTimerMap().get(number);
        if(timer != null) {
            timer.cancel();
            timer = new Timer();
            timer.schedule(new RemoveFailedNode(number, nodesConfig), 2000);
            nodesConfig.getTimerMap().put(number, timer);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping(ELECTION_PATH)
    public ResponseEntity<Void> elect(@PathVariable int fromNode) {
        if (fromNode < nodesConfig.getSelf()) {
            return ResponseEntity.accepted().build();
        }
        return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).build();
    }

    @GetMapping(ELECTED_PATH)
    public ResponseEntity<Void> elected(@PathVariable int newLeader) {
        log.info("Self: {}. New Leader elected: {}", nodesConfig.getSelf(), newLeader);
        nodesConfig.setNewLeader(newLeader);
        return ResponseEntity.ok().build();
    }
    @GetMapping(REMOVE_NODE)
    public ResponseEntity<Void> removeNode(@PathVariable int node) {
        log.info("Self: {}. Request to remove node: {}", nodesConfig.getSelf(), node);
        nodesConfig.removeNode(node);
        return ResponseEntity.ok().build();
    }
    @GetMapping(ADD_NODE)
    public ResponseEntity<Integer> addNode(@PathVariable("node") int node, @PathVariable("port") int port) {
        log.info("Request to add node: {} with port: {}", node, port);
        String address = "localhost:" + port;
        nodeAddService.addNode(node, port);
        nodesConfig.addNode(node, address);
        if(nodesConfig.isLeader()) {
            Timer timer = new Timer();
            RemoveFailedNode removeFailedNode = new RemoveFailedNode(node, nodesConfig);
            nodesConfig.getTimerMap().put(node, timer);
            timer.schedule(removeFailedNode, 3000);
        }
        return ResponseEntity.ok(node);
    }
    @GetMapping(UPDATE_QUEUE_PATH)
    public ResponseEntity<Void> updateQueue(@PathVariable String documentId, @PathVariable String user, @PathVariable int fromNode) {
        if(fromNode != nodesConfig.getSelf()) {
            nodesConfig.addToQueue(documentId, user);
        }
        return ResponseEntity.ok().build();
    }
}