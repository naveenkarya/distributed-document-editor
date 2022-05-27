package coen317.project.documenteditor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
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
                    System.out.println(document.getCreatedDate());
                    System.out.println(document.getAuthor());
                    System.out.println("Replicated");
                } catch (Exception e) {
                    System.out.println("Unable to replicate to node: " + entry.getKey() + e.getMessage());
                }
            });
        }
    }

    public void replicate(String content, String documentId) {
        if (nodesInfo.isLeader()) {
            nodesInfo.getNodeMap().entrySet().forEach(entry -> {
                try {
                    restTemplate.postForEntity(entry.getValue() + "/document/"+documentId, content, WordDocument.class);
                    System.out.println("Replicated");
                } catch (Exception e) {
                    System.out.println("Unable to replicate to node: " + entry.getKey() + e.getMessage());
                }
            });
        }
    }
}
