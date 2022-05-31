package coen317.project.documenteditor;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static coen317.project.documenteditor.DocumentController.DOCUMENT_ADD_PATH;
import static coen317.project.documenteditor.NodeController.ADD_NODE;

@Service
@Slf4j
public class NodeAddService {
    RestTemplate restTemplate = new RestTemplate();
    @Autowired
    NodesInfo nodesInfo;

    public void addNode(int nodeId, int port) {
        if (nodesInfo.isLeader()) {
            nodesInfo.getNodeMap().entrySet().forEach(entry -> {
                try {
                    String[] hostAndPort = entry.getValue().split(":");
                    log.info("Replicating node Addition {} to: {}", nodeId, entry.getKey());
                    String url = UriComponentsBuilder.newInstance()
                            .scheme("http").host(hostAndPort[0]).port(hostAndPort[1])
                            .path(ADD_NODE).buildAndExpand(nodeId,port).toUriString();
                    restTemplate.getForEntity(url, WordDocument.class);
                    log.info("Replicated Nodes");
                } catch (Exception ce) {
                    log.info("Unable to replicate the addition: " + entry.getKey() + ce.getMessage());
                }
            });
        }
    }
}
