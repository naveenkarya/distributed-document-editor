package coen317.project.documenteditor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
public class DocumentController {

    public static final String DOCUMENT_ADD_PATH = "/document/add";
    public static final String DOCUMENT_UPDATE_PATH = "/document/{documentId}";
    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    ReplicationService replicationService;

    @PostMapping(DOCUMENT_ADD_PATH)
    public ResponseEntity<WordDocument> addDocument(@RequestBody WordDocument document) {
        document.setVersion(null);
        WordDocument savedDocument = documentRepository.save(document);
        replicationService.replicate(savedDocument);
        return ResponseEntity.ok(document);

    }

    @PostMapping(DOCUMENT_UPDATE_PATH)
    public ResponseEntity<WordDocument> updateDocument(@RequestBody String content, @PathVariable String documentId) {
        Optional<WordDocument> doc = documentRepository.findById(documentId);
        if (doc.isPresent()) {
            WordDocument wordDocument = doc.get();
            wordDocument.setContent(content);
            documentRepository.save(wordDocument);
            replicationService.replicate(content, documentId);
            return ResponseEntity.ok(wordDocument);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(DOCUMENT_UPDATE_PATH)
    public ResponseEntity<WordDocument> getDocument(@PathVariable String documentId) {
        return ResponseEntity.ok(documentRepository.findById(documentId).get());
    }

}