package coen317.project.documenteditor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

import static coen317.project.documenteditor.NodeController.UPDATE_QUEUE_PATH;

@RestController
@Slf4j
public class DocumentController {

    public static final String DOCUMENT_ADD_PATH = "/document/add";
    public static final String DOCUMENT_UPDATE_PATH = "/document/{documentId}";

    public static final String DOCUMENT_GET_ALL_PATH = "/document/all";

    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    ReplicationService replicationService;
    @Autowired
    NodesInfo nodesInfo;
    RestTemplate restTemplate = new RestTemplate();

    @PostMapping(DOCUMENT_ADD_PATH)
    public ResponseEntity<WordDocument> addDocument(@RequestBody WordDocument document) {
        document.setVersion(null);
        WordDocument savedDocument = documentRepository.save(document);
        replicationService.replicate(savedDocument);
        return ResponseEntity.ok(document);

    }

    @PostMapping(DOCUMENT_UPDATE_PATH)
    public ResponseEntity<WordDocument> updateDocument(@RequestBody WordDocument document, @PathVariable String documentId) {
        Optional<WordDocument> doc = documentRepository.findById(documentId);
        if (doc.isPresent()) {
            WordDocument wordDocument = doc.get();
            wordDocument.setContent(document.content);
            wordDocument.setTitle(document.title);
            documentRepository.save(wordDocument);
            replicationService.replicate(document, documentId);
            return ResponseEntity.ok(wordDocument);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(DOCUMENT_UPDATE_PATH)
    public ResponseEntity<WordDocument> getDocument(@PathVariable String documentId, @RequestParam boolean edit, @RequestParam String user) {
        Optional<WordDocument> doc = documentRepository.findById(documentId);
        if(!doc.isPresent()) return ResponseEntity.notFound().build();
        WordDocument wordDocument = doc.get();
        if(edit == true) {
            nodesInfo.getNodeMap().entrySet().forEach(entry -> {
                try {
                    String[] hostAndPort = entry.getValue().split(":");
                    log.info("Adding user {} to the queue of node: {}", user, entry.getKey());
                    String url = UriComponentsBuilder.newInstance()
                            .scheme("http").host(hostAndPort[0]).port(hostAndPort[1])
                            .path(UPDATE_QUEUE_PATH).buildAndExpand(documentId, user, nodesInfo.getSelf()).toUriString();
                    restTemplate.getForEntity(url, Void.class);
                } catch (Exception e) {
                    log.info("Unable to update queue of node {}" + entry.getKey());
                }
            });
            nodesInfo.addToQueue(documentId, user);
            if(nodesInfo.getDocUserQueue().get(documentId).size() == 1) {
                wordDocument.setLocked(true);
            }
        }
        return ResponseEntity.ok(wordDocument);
    }

    @GetMapping(DOCUMENT_GET_ALL_PATH)
    public ResponseEntity<List> getDocument() {
        List<WordDocument> documentList = documentRepository.findAll();
        return ResponseEntity.ok(documentList);
    }

}