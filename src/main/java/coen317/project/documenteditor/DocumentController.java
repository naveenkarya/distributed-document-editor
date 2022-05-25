package coen317.project.documenteditor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
public class DocumentController {

    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    ReplicationService replicationService;

    @PostMapping("/document/add")
    public ResponseEntity<WordDocument> addDocument(@RequestBody WordDocument document) {
        WordDocument savedDocument = documentRepository.save(document);
        replicationService.replicate(savedDocument);
        return ResponseEntity.ok(document);

    }

    @PostMapping("/document/{documentId}")
    public ResponseEntity<WordDocument> updateDocument(@RequestBody String content, @PathVariable String documentId) {
        Optional<WordDocument> doc = documentRepository.findById(documentId);
        if (doc.isPresent()) {
            WordDocument wordDocument = doc.get();
            wordDocument.setContent(content);
            return ResponseEntity.ok(wordDocument);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/document/{documentId}")
    public ResponseEntity<WordDocument> getDocument(@PathVariable String documentId) {
        return ResponseEntity.ok(documentRepository.findById(documentId).get());
    }

}