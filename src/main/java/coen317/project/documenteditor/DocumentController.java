package coen317.project.documenteditor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class DocumentController {

    public static final String DOCUMENT_ADD_PATH = "/document/add";
    public static final String DOCUMENT_UPDATE_PATH = "/document/{documentId}";

    public static final String DOCUMENT_GET_ALL_PATH = "/document/all";

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
    public ResponseEntity<WordDocument> updateDocument(@RequestBody WordDocument document, @PathVariable String documentId) {
        Optional<WordDocument> doc = documentRepository.findById(documentId);
        if (doc.isPresent()) {
            WordDocument wordDocument = doc.get();
            System.out.println(document.content);
            wordDocument.setContent(document.content);
            wordDocument.setTitle(document.title);
            documentRepository.save(wordDocument);
            replicationService.replicate(document, documentId);
            return ResponseEntity.ok(wordDocument);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(DOCUMENT_UPDATE_PATH)
    public ResponseEntity<WordDocument> getDocument(@PathVariable String documentId) {
        return ResponseEntity.ok(documentRepository.findById(documentId).get());
    }

    @GetMapping(DOCUMENT_GET_ALL_PATH)
    public ResponseEntity<List> getDocument() {
        List<WordDocument> documentList = documentRepository.findAll();
        return ResponseEntity.ok(documentList);
    }

}