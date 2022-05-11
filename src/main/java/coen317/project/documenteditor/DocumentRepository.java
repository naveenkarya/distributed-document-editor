package coen317.project.documenteditor;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DocumentRepository extends MongoRepository<WordDocument, String> {

    //public Document findByFirstName(String firstName);
    //public List<Document> findByLastName(String lastName);

}