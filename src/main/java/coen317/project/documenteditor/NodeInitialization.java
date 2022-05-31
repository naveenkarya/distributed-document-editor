package coen317.project.documenteditor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class NodeInitialization {

    @Autowired
    private NodesInfo nodesInfo;
    @Autowired
    FollowerStatusService followerStatusService;
    private static final String LB_ADD_NODE_PATH = "/node/add/{port}";

    RestTemplate restTemplate = new RestTemplate();

    @EventListener(ApplicationReadyEvent.class)
    public void initializeNode() {
        if (nodesInfo.isLeader()) {
            String url = UriComponentsBuilder.newInstance()
                    .scheme("http").host(nodesInfo.getLoadBalancer())
                    .path(LB_ADD_NODE_PATH).buildAndExpand(nodesInfo.getSelfPort()).toUriString();
            ResponseEntity<Integer> selfNode = restTemplate.getForEntity(url, Integer.class);
            nodesInfo.setSelf(selfNode.getBody());
        }
    }

}
