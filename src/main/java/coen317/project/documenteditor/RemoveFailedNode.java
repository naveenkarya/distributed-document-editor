package coen317.project.documenteditor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.TimerTask;

import static coen317.project.documenteditor.NodeController.REMOVE_NODE;

@Slf4j
public class RemoveFailedNode extends TimerTask {
    int node;
    NodesInfo nodesInfo;

    public RemoveFailedNode(int node, NodesInfo nodesInfo) {
        super();
        this.node = node;
        this.nodesInfo = nodesInfo;
    }

    @Override
    public void run() {
        log.info("Failure detected for follower node {}", node);
        RestTemplate restTemplate = new RestTemplate();
        nodesInfo.removeNode(node);
        for (Map.Entry<Integer, String> entry : nodesInfo.getNodeMap().entrySet()) {
            String uri = UriComponentsBuilder.newInstance()
                    .scheme("http").host(entry.getValue())
                    .path(REMOVE_NODE).buildAndExpand(node).toUriString();
            try {
                restTemplate.getForEntity(uri, Void.class);
            } catch (Exception ce) {
                log.info("Cannot connect to node: " + entry.getKey());
            }
            // TODO: Update Load Balancer - remove node
        }
    }

}
