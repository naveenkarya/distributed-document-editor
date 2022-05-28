package coen317.project.documenteditor;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Map;

public interface DocumentRepository extends MongoRepository<WordDocument, String> {

}