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

    @Autowired
    NodesConfig nodesConfig;
    public static final String PING_PATH = "/ping/{number}";
    public static final String ELECTION_PATH = "/elect/{fromNode}";
    public static final String ELECTED_PATH = "/elected/{newLeader}";
    public static final String REMOVE_NODE = "/remove/{node}";

    public static final String ADD_NODE = "/add/{node}/{port}";

    @Autowired
    NodeAddService nodeAddService;

    /**
     * Receives ping from followers
     * @param number ID of the node that sent this ping
     * @return
     */
    @GetMapping(PING_PATH)
    public ResponseEntity<Void> ping(@PathVariable int number) {
        // Reset the timer for the follower that pinged the leader
        Timer timer = nodesConfig.getTimerMap().get(number);
        if(timer != null) {
            timer.cancel();
            timer = new Timer();
            timer.schedule(new RemoveFailedNode(number, nodesConfig), 2000);
            nodesConfig.getTimerMap().put(number, timer);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Received election message from node with ID: "fromNode".
     * Reply accepted only if the received ID is less than self node ID.
     * @param fromNode ID of the node that sent this election message
     * @return
     */
    @GetMapping(ELECTION_PATH)
    public ResponseEntity<Void> elect(@PathVariable int fromNode) {
        if (fromNode < nodesConfig.getSelf()) {
            return ResponseEntity.accepted().build();
        }
        return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).build();
    }

    /**
     * Receive elected message from the newly elected leader. Receiving node updates its config.
     * @param newLeader ID of the new leader
     * @return
     */
    @GetMapping(ELECTED_PATH)
    public ResponseEntity<Void> elected(@PathVariable int newLeader) {
        log.info("Self: {}. New Leader elected: {}", nodesConfig.getSelf(), newLeader);
        nodesConfig.setNewLeader(newLeader);
        return ResponseEntity.ok().build();
    }

    /**
     * Receive request to remove a node from the list because failure has been detected for that node
     * @param node ID of the node that failed and need to be removed from configuration
     * @return
     */
    @GetMapping(REMOVE_NODE)
    public ResponseEntity<Void> removeNode(@PathVariable int node) {
        log.info("Self: {}. Request to remove node: {}", nodesConfig.getSelf(), node);
        nodesConfig.removeNode(node);
        return ResponseEntity.ok().build();
    }

    /**
     * Receive request to add a new node to this distributed system
     * @param node ID of the new node
     * @param port port of the new node
     * @return
     */
    @GetMapping(ADD_NODE)
    public ResponseEntity<Integer> addNode(@PathVariable("node") int node, @PathVariable("port") int port) {
        log.info("Request to add node: {} with port: {}", node, port);
        String address = "localhost:" + port;
        nodeAddService.addNode(node, port);
        nodesConfig.addNode(node, address);
        // If request received from leader, add a timer to track pings from this new follower
        if(nodesConfig.isLeader()) {
            Timer timer = new Timer();
            RemoveFailedNode removeFailedNode = new RemoveFailedNode(node, nodesConfig);
            nodesConfig.getTimerMap().put(node, timer);
            // 3 second is initial timer because the node just started, subsequent timers will be 2 seconds.
            timer.schedule(removeFailedNode, 3000);
        }
        return ResponseEntity.ok(node);
    }
}