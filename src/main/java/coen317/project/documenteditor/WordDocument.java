package coen317.project.documenteditor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.Date;

@Document
@Getter
@Setter
public class WordDocument {
    @Id
    public String id;

    public String author;

    public String title;

    @CreatedDate
    private Instant createdDate = Instant.now();


    @Version
    private Long version;
    public String content;

    public WordDocument() {
    }

    public WordDocument(String author, String title, String content) {
        this.author = author;
        this.title = title;
        this.content = content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
