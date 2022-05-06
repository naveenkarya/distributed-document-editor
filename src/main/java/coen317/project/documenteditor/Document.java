package coen317.project.documenteditor;

import org.springframework.data.annotation.Id;


public class Document {

    @Id
    public String id;

    public String content;

    public Document() {
    }

    public Document(String content) {
        this.content = content;
    }

}