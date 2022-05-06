package coen317.project.documenteditor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class DocumentController {

    @Autowired
    DocumentRepository documentRepository;

    @PostMapping("/document/add")
    public ResponseEntity<String> addDocument(@RequestBody Document document) {
        documentRepository.save(document);
        return ResponseEntity.ok(document.id);
    }

    @GetMapping("/document/{documentId}")
    public ResponseEntity<Document> getDocument(@PathVariable String documentId) {
        return ResponseEntity.ok(documentRepository.findById(documentId).get());
    }
}