package coen317.project.documenteditor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

import static coen317.project.documenteditor.NodeController.ELECTED_PATH;
import static coen317.project.documenteditor.NodeController.ELECTION_PATH;

@Service
@Slf4j
public class LeaderElectionService {

    RestTemplate restTemplate = new RestTemplate();
    @Autowired
    NodesConfig nodesConfig;
    private static final String LB_LEADER_PATH = "/node/leader/{leader}";

    public void electNewLeader() {
        int count = 0;
        for (Map.Entry<Integer, String> entry : nodesConfig.getNodeMap().entrySet()) {
            // Send election message only to nodes with ID higher than self node ID
            if (entry.getKey() > nodesConfig.getSelf()) {
                String uri = UriComponentsBuilder.newInstance()
                        .scheme("http").host(entry.getValue())
                        .path(ELECTION_PATH).buildAndExpand(nodesConfig.getSelf()).toUriString();
                log.info("Sending election message to " + uri);
                try {
                    ResponseEntity<Void> response = restTemplate.getForEntity(uri, Void.class);
                    if (response.getStatusCode() == HttpStatus.ACCEPTED) {
                        count++;
                    }
                } catch (Exception ce) {
                    log.info("Cannot connect to node: " + entry.getKey());
                }
            }
        }
        // If no accepted response received, elect itself as the leader
        if (count == 0) {
            log.info("Electing self: {} as the new leader.", nodesConfig.getSelf());
            nodesConfig.setNewLeader(nodesConfig.getSelf());
            nodesConfig.createPingTimers();
            // Inform other nodes of the new leader with elected message
            for (Map.Entry<Integer, String> entry : nodesConfig.getNodeMap().entrySet()) {
                String url = UriComponentsBuilder.newInstance()
                        .scheme("http").host(entry.getValue())
                        .path(ELECTED_PATH).buildAndExpand(nodesConfig.getSelf()).toUriString();
                log.info("Sending elected message to " + entry.getKey());
                try {
                    restTemplate.getForEntity(url, Void.class);
                } catch (Exception ce) {
                    log.info("Cannot connect to node: " + entry.getKey());
                }
            }
            // Update new leader in Load Balancer
            String lbUrl = UriComponentsBuilder.newInstance()
                    .scheme("http").host(nodesConfig.getLoadBalancer())
                    .path(LB_LEADER_PATH).buildAndExpand(nodesConfig.getSelf()).toUriString();
            log.info("Sending elected message to Load balancer");
            try {
                restTemplate.getForEntity(lbUrl, Void.class);
            } catch (Exception ce) {
                log.info("Cannot connect to Load Balancer");
            }
        }
    }

}
