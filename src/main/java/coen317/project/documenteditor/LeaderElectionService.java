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
import static coen317.project.documenteditor.NodeController.ELECT_PATH;

@Service
@Slf4j
public class LeaderElectionService {

    RestTemplate restTemplate = new RestTemplate();
    @Autowired
    NodesInfo nodesInfo;

    public void electNewLeader() {
        int count = 0;
        for (Map.Entry<Integer, String> entry : nodesInfo.getNodeMap().entrySet()) {
            if (entry.getKey() <= nodesInfo.getSelf()) continue;
            String uri = UriComponentsBuilder.newInstance()
                    .scheme("http").host(entry.getValue())
                    .path(ELECT_PATH).buildAndExpand(nodesInfo.getSelf()).toUriString();
            System.out.println("Sending election message to " + uri);
            try {
                ResponseEntity<Void> response = restTemplate.getForEntity(uri, Void.class);
                if (response.getStatusCode() == HttpStatus.ACCEPTED) {
                    count++;
                }
            } catch (Exception ce) {
                System.out.println("Cannot connect to node: " + entry.getKey());
            }
        }
        if (count == 0) {
            log.info("Electing self: {} as the new leader.", nodesInfo.getSelf());
            nodesInfo.setNewLeader(nodesInfo.getSelf());
            for (Map.Entry<Integer, String> entry : nodesInfo.getNodeMap().entrySet()) {
                String uri = UriComponentsBuilder.newInstance()
                        .scheme("http").host(entry.getValue())
                        .path(ELECTED_PATH).buildAndExpand(nodesInfo.getSelf()).toUriString();
                System.out.println("Sending elected message to " + entry.getKey());
                try {
                    restTemplate.getForEntity(uri, Void.class);
                } catch (Exception ce) {
                    System.out.println("Cannot connect to node: " + entry.getKey());
                }
            }
        }
    }

}