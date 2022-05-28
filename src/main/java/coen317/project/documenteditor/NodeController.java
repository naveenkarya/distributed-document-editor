package coen317.project.documenteditor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Timer;

@RestController
@Slf4j
public class NodeController {
    @Autowired
    NodesInfo nodesInfo;
    public static final String PING_PATH = "/ping/{number}";
    public static final String ELECTION_PATH = "/elect/{fromNode}";
    public static final String ELECTED_PATH = "/elected/{newLeader}";
    public static final String REMOVE_NODE = "/remove/{node}";

    @GetMapping(PING_PATH)
    public ResponseEntity<Void> ping(@PathVariable int number) {
        //log.info("Ping from {}", number);
        Timer timer = nodesInfo.getTimerMap().get(number);
        if(timer != null) {
            timer.cancel();
            timer = new Timer();
            timer.schedule(new RemoveFailedNode(number, nodesInfo), 2000);
            nodesInfo.getTimerMap().put(number, timer);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping(ELECTION_PATH)
    public ResponseEntity<Void> elect(@PathVariable int fromNode) {
        if (fromNode < nodesInfo.getSelf()) {
            return ResponseEntity.accepted().build();
        }
        return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).build();
    }

    @GetMapping(ELECTED_PATH)
    public ResponseEntity<Void> elected(@PathVariable int newLeader) {
        log.info("Self: {}. New Leader elected: {}", nodesInfo.getSelf(), newLeader);
        nodesInfo.setNewLeader(newLeader);
        return ResponseEntity.ok().build();
    }
    @GetMapping(REMOVE_NODE)
    public ResponseEntity<Void> removeNode(@PathVariable int node) {
        log.info("Self: {}. Request to remove node: {}", nodesInfo.getSelf(), node);
        nodesInfo.removeNode(node);
        return ResponseEntity.ok().build();
    }
}