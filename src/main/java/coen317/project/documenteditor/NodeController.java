package coen317.project.documenteditor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NodeController {

    @GetMapping("/ping/{number}")
    public ResponseEntity<Void> ping(@PathVariable String number) {
        System.out.println("Ping from " + number);
        return ResponseEntity.ok().build();
    }
}