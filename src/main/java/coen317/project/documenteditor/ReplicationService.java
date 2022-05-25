package coen317.project.documenteditor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static coen317.project.documenteditor.DocumentEditorApplication.leader;

@Service
public class ReplicationService {
    RestTemplate restTemplate = new RestTemplate();

    @Value("#{${nodeMap}}")
    private Map<Integer,String> nodeMap;

    @Value("${self}")
    int self;
    @Async
    public void replicate(WordDocument document) {
        if (leader == self) {
            nodeMap.entrySet().forEach(entry -> {
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
        if (leader == self) {
            nodeMap.entrySet().forEach(entry -> {
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
