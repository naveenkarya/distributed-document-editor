package coen317.project.documenteditor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ReplicationService {
    RestTemplate restTemplate = new RestTemplate();

    @Autowired
    NodesInfo nodesInfo;
    @Async
    public void replicate(WordDocument document) {
        if (nodesInfo.isLeader()) {
            nodesInfo.getNodeMap().entrySet().forEach(entry -> {
                try {
                    restTemplate.postForEntity(entry.getValue() + "/document/add", document, WordDocument.class);
                    log.info("Replicated");
                } catch (Exception e) {
                    log.info("Unable to replicate to node: " + entry.getKey() + e.getMessage());
                }
            });
        }
    }
    @Async
    public void replicate(String content, String documentId) {
        if (nodesInfo.isLeader()) {
            nodesInfo.getNodeMap().entrySet().forEach(entry -> {
                try {
                    restTemplate.postForEntity(entry.getValue() + "/document/"+documentId, content, WordDocument.class);
                    log.info("Replicated");
                } catch (Exception e) {
                    log.info("Unable to replicate to node: " + entry.getKey() + e.getMessage());
                }
            });
        }
    }
}
