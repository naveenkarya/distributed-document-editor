package coen317.project.documenteditor;

import jdk.swing.interop.SwingInterOpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@Slf4j
public class DocumentController {

    public static final String DOCUMENT_ADD_PATH = "/document/add";
    public static final String DOCUMENT_UPDATE_PATH = "/document/{documentId}";

    public static final String DOCUMENT_GET_ALL_PATH = "/document/all";

    public static final String UPDATE_QUEUE_PATH = "/updateQueue/{documentId}/{user}/{fromNode}";
    private Map<String, Object> locks = new ConcurrentHashMap<>();
    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    ReplicationService replicationService;
    @Autowired
    NodesConfig nodesConfig;
    RestTemplate restTemplate = new RestTemplate();

    @PostMapping(DOCUMENT_ADD_PATH)
    public ResponseEntity<WordDocument> addDocument(@RequestBody WordDocument document) {
//        document.setVersion(null);
        WordDocument savedDocument = documentRepository.save(document);
        replicationService.replicate(savedDocument);
        return ResponseEntity.ok(document);

    }

    @PostMapping(DOCUMENT_UPDATE_PATH)
    public ResponseEntity<WordDocument> updateDocument(@RequestBody WordDocument document, @PathVariable String documentId, @RequestParam String user) {
        Optional<WordDocument> doc = documentRepository.findById(documentId);
        if (doc.isPresent()) {
            WordDocument wordDocument = doc.get();
            wordDocument.setContent(document.content);
            wordDocument.setTitle(document.title);
            if(nodesConfig.isLeader()) {
                if (user.equals(wordDocument.getLockedBy())) {
                    documentRepository.save(wordDocument);
                    replicationService.replicate(document, documentId, user);
                    return ResponseEntity.ok(wordDocument);
                }
                return ResponseEntity.status(HttpStatus.CONFLICT).body(wordDocument);
            }
            else {
                wordDocument.setLockedBy(document.getLockedBy());
                wordDocument.setLocked(document.isLocked());
                documentRepository.save(wordDocument);
                return ResponseEntity.ok(wordDocument);
            }
        }
        return ResponseEntity.notFound().build();
    }


    @GetMapping(DOCUMENT_UPDATE_PATH)
    public ResponseEntity<WordDocument> getDocument(@PathVariable String documentId, @RequestParam(required = false, defaultValue = "false") boolean edit, @RequestParam Optional<String> user) {
        Optional<WordDocument> doc = documentRepository.findById(documentId);
        if (!doc.isPresent()) return ResponseEntity.notFound().build();
        WordDocument wordDocument = doc.get();
        if (edit == true) {
            if(!user.isPresent()) {
                return ResponseEntity.badRequest().build();
            }
            Boolean canEdit = false;
            if(nodesConfig.isLeader()) {
                canEdit = updateQueue(documentId, user.get(), nodesConfig.getSelf()).getBody();
            }
            else {
                String[] hostAndPort = nodesConfig.getNodeMap().get(nodesConfig.getLeader()).split(":");
                try {
                    log.info("Updating queue in leader for documentId {}", documentId);
                    String url = UriComponentsBuilder.newInstance()
                            .scheme("http").host(hostAndPort[0]).port(hostAndPort[1])
                            .path(UPDATE_QUEUE_PATH).buildAndExpand(documentId, user.get(), nodesConfig.getSelf()).toUriString();
                    canEdit = restTemplate.getForEntity(url, Boolean.class).getBody();
                } catch (Exception e) {
                    log.info("Unable to update queue in leader");
                }
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
        log.info("IsLeader: {}", nodesConfig.isLeader());
        if (nodesConfig.isLeader()) {
            synchronized (locks.computeIfAbsent(documentId, doc -> new Object())) {
                WordDocument wordDocument = documentRepository.findById(documentId).get();
                log.info("islocked: {}, locked by: {}, user requesting: {}", wordDocument.isLocked(), wordDocument.getLockedBy(), user);
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
                    replicationService.replicate(wordDocument, documentId, user);
                    canEdit = true;
                }
            }
        }
        log.info("current queue: {}", nodesConfig.getDocUserQueue().get(documentId));
        return ResponseEntity.ok().body(canEdit);
    }

    @PostMapping("/document/releaseLock/{documentId}")
    public ResponseEntity<WordDocument> releaseLock(@PathVariable String documentId) {
        Optional<WordDocument> doc = documentRepository.findById(documentId);
        if (doc.isPresent()) {
            synchronized (locks.computeIfAbsent(documentId, d -> new Object())) {
                WordDocument wordDocument = doc.get();
                if(nodesConfig.isQueueEmpty(documentId)) {
                    wordDocument.setLocked(false);
                    wordDocument.setLockedBy(null);
                }
                else {
                    wordDocument.setLockedBy(nodesConfig.getNextUser(documentId));
                    log.info("Queue updated to : {}", nodesConfig.getDocUserQueue().get(documentId));
                }
                documentRepository.save(wordDocument);
                replicationService.replicate(wordDocument, documentId, "NA");
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.notFound().build();
    }

}