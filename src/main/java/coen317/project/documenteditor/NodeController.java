package coen317.project.documenteditor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class NodeController {
    @Autowired
    NodesInfo nodesInfo;
    public static final String PING_PATH = "/ping/{number}";
    public static final String ELECT_PATH = "/elect/{fromNode}";
    public static final String ELECTED_PATH = "/elected/{newLeader}";

    @GetMapping(PING_PATH)
    public ResponseEntity<Void> ping(@PathVariable String number) {
        System.out.println("Ping from " + number);
        return ResponseEntity.ok().build();
    }

    @GetMapping(ELECT_PATH)
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
}