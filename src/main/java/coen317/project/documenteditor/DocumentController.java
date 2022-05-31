package coen317.project.documenteditor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
public class DocumentController {

    public static final String DOCUMENT_ADD_PATH = "/document/add";
    public static final String DOCUMENT_UPDATE_PATH = "/document/{documentId}";

    public static final String DOCUMENT_GET_ALL_PATH = "/document/all";
    public static final String UPDATE_QUEUE_PATH = "/updateQueue/{documentId}/{user}/{fromNode}";
    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    ReplicationService replicationService;
    @Autowired
    NodesConfig nodesConfig;
    RestTemplate restTemplate = new RestTemplate();

    @PostMapping(DOCUMENT_ADD_PATH)
    public ResponseEntity<WordDocument> addDocument(@RequestBody WordDocument document) {
        document.setVersion(null);
        WordDocument savedDocument = documentRepository.save(document);
        replicationService.replicate(savedDocument);
        return ResponseEntity.ok(document);

    }

    @PostMapping(DOCUMENT_UPDATE_PATH)
    public ResponseEntity<WordDocument> updateDocument(@RequestBody WordDocument document, @PathVariable String documentId, @RequestParam String user) {
        Optional<WordDocument> doc = documentRepository.findById(documentId);
        if (doc.isPresent()) {
            WordDocument wordDocument = doc.get();
            if (user != null && user.equals(wordDocument.getLockedBy())) {
                wordDocument.setContent(document.content);
                wordDocument.setTitle(document.title);
                if (nodesConfig.getDocUserQueue().get(documentId).isEmpty()) {
                    wordDocument.setLocked(false);
                    wordDocument.setLockedBy(null);
                } else {
                    wordDocument.setLockedBy(nodesConfig.getNextUser(documentId));
                }
                documentRepository.save(wordDocument);
                replicationService.replicate(document, documentId);
                return ResponseEntity.ok(wordDocument);
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(wordDocument);
        }
        return ResponseEntity.notFound().build();
    }


    @GetMapping(DOCUMENT_UPDATE_PATH)
    public ResponseEntity<WordDocument> getDocument(@PathVariable String documentId, @RequestParam boolean edit, @RequestParam String user) {
        Optional<WordDocument> doc = documentRepository.findById(documentId);
        if (!doc.isPresent()) return ResponseEntity.notFound().build();
        WordDocument wordDocument = doc.get();
        if (edit == true) {
            Boolean canEdit = false;
            String[] hostAndPort = nodesConfig.getNodeMap().get(nodesConfig.getLeader()).split(":");
            try {
                log.info("Updating queue in leader for documentId {}", documentId);
                String url = UriComponentsBuilder.newInstance()
                        .scheme("http").host(hostAndPort[0]).port(hostAndPort[1])
                        .path(UPDATE_QUEUE_PATH).buildAndExpand(documentId, user, nodesConfig.getSelf()).toUriString();
                canEdit = restTemplate.getForEntity(url, Boolean.class).getBody();
            } catch (Exception e) {
                log.info("Unable to update queue in leader");
            }
            if (!canEdit) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(wordDocument);
            }
        }
        return ResponseEntity.ok(wordDocument);
    }

    @GetMapping(DOCUMENT_GET_ALL_PATH)
    public ResponseEntity<List> getDocument() {
        List<WordDocument> documentList = documentRepository.findAll();
        return ResponseEntity.ok(documentList);
    }

    @GetMapping(UPDATE_QUEUE_PATH)
    public ResponseEntity<Boolean> updateQueue(@PathVariable String documentId, @PathVariable String user, @PathVariable int fromNode) {
        boolean canEdit = false;
        if (nodesConfig.isLeader()) {
            synchronized (this) {
                WordDocument wordDocument = documentRepository.findById(documentId).get();
                if (wordDocument.isLocked()) {
                    if(wordDocument.getLockedBy().equals(user)) {
                        canEdit = true;
                    }
                    else {
                        nodesConfig.addToQueue(documentId, user);
                    }
                } else {
                    wordDocument.setLockedBy(user);
                    wordDocument.setLocked(true);
                    documentRepository.save(wordDocument);
                    canEdit = true;
                }
            }
        }
        return ResponseEntity.ok().body(canEdit);
    }

}