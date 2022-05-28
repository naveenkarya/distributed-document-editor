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
    NodesInfo nodesInfo;
    private static final String LB_LEADER_PATH = "/node/leader/{leader}";

    public void electNewLeader() {
        int count = 0;
        for (Map.Entry<Integer, String> entry : nodesInfo.getNodeMap().entrySet()) {
            if (entry.getKey() > nodesInfo.getSelf()) {
                String uri = UriComponentsBuilder.newInstance()
                        .scheme("http").host(entry.getValue())
                        .path(ELECTION_PATH).buildAndExpand(nodesInfo.getSelf()).toUriString();
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
        if (count == 0) {
            log.info("Electing self: {} as the new leader.", nodesInfo.getSelf());
            nodesInfo.setNewLeader(nodesInfo.getSelf());
            for (Map.Entry<Integer, String> entry : nodesInfo.getNodeMap().entrySet()) {
                String url = UriComponentsBuilder.newInstance()
                        .scheme("http").host(entry.getValue())
                        .path(ELECTED_PATH).buildAndExpand(nodesInfo.getSelf()).toUriString();
                log.info("Sending elected message to " + entry.getKey());
                try {
                    restTemplate.getForEntity(url, Void.class);
                } catch (Exception ce) {
                    log.info("Cannot connect to node: " + entry.getKey());
                }
                // Update Leader in Load Balancer
                String lbUrl = UriComponentsBuilder.newInstance()
                        .scheme("http").host(nodesInfo.getLoadBalancer())
                        .path(LB_LEADER_PATH).buildAndExpand(nodesInfo.getSelf()).toUriString();
                log.info("Sending elected message to Load balancer");
                try {
                    restTemplate.getForEntity(lbUrl, Void.class);
                } catch (Exception ce) {
                    log.info("Cannot connect to Load Balancer");
                }
            }
        }
    }

}
