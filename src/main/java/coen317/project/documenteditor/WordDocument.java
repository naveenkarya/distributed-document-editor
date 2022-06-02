package coen317.project.documenteditor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Document
@Getter
@Setter
@ToString
public class WordDocument {
    @Id
    public String id;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String author;

    public String title;

    @CreatedDate
    private LocalDate createdDate = LocalDate.now();

    @LastModifiedDate
    private LocalDate lastModified = LocalDate.now();

//    @Version
//    private Long version;
    public String content;
    public boolean locked = false;
    public String lockedBy;

    public WordDocument() {
    }

    public WordDocument(String author, String title, String content) {
        this.author = author;
        this.title = title;
        this.content = content;
    }

    public WordDocument(WordDocument document) {
        this.author = document.author;
        this.title = document.title;
        this.content = document.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
