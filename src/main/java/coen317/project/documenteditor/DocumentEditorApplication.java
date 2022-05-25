package coen317.project.documenteditor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DocumentEditorApplication {
	public static int leader = 1;
	public static void main(String[] args) {
		SpringApplication.run(DocumentEditorApplication.class, args);
	}

}
