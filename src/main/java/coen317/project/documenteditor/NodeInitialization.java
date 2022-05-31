package coen317.project.documenteditor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.stream.Collectors;

@Service
@Slf4j
public class NodeInitialization {

    @Autowired
    private NodesConfig nodesConfig;
    @Autowired
    FollowerStatusService followerStatusService;
    private static final String LB_ADD_NODE_PATH = "/node/add/{port}";

    RestTemplate restTemplate = new RestTemplate();

    @EventListener(ApplicationReadyEvent.class)
    public void initializeNode() {
        if (nodesConfig.getSelf() == 0) {
            updateExistingNodes();
            addSelf();
        }
    }

    private void updateExistingNodes() {
        String[] lbHostPort = nodesConfig.getLoadBalancer().split(":");
        String url = UriComponentsBuilder.newInstance()
                .scheme("http").host(lbHostPort[0]).port(lbHostPort[1])
                .path("/node/map").toUriString();
        NodeInfoResponse nodeInfoResponse = restTemplate.getForEntity(url, NodeInfoResponse.class).getBody();
        nodesConfig.setLeader(nodeInfoResponse.getLeader());
        nodesConfig.setNodeMap(nodeInfoResponse.getNodeMap().entrySet().stream()
                .collect(Collectors.toConcurrentMap(e -> e.getKey(), e -> "localhost:" + e.getValue())));
    }

    private void addSelf() {
        String[] lbHostPort = nodesConfig.getLoadBalancer().split(":");
        String url = UriComponentsBuilder.newInstance()
                .scheme("http").host(lbHostPort[0]).port(lbHostPort[1])
                .path(LB_ADD_NODE_PATH).buildAndExpand(nodesConfig.getSelfPort()).toUriString();
        ResponseEntity<Integer> selfNode = restTemplate.getForEntity(url, Integer.class);
        nodesConfig.setSelf(selfNode.getBody());
    }

}
