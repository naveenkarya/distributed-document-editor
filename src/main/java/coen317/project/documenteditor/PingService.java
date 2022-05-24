package coen317.project.documenteditor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

@Service
public class PingService {
    RestTemplate restTemplate = new RestTemplate();

    @Value("#{${nodeMap}}")
    private Map<Integer,String> nodeMap;

    @Value("${self}")
    int self;


    @Scheduled(fixedRate = 1000)
    public void sendPingTo() {
        nodeMap.entrySet().forEach(entry -> {
            System.out.println("Sending ping to " + entry.getValue());
            try{
                restTemplate.getForObject(entry.getValue() + "/ping/" + self, String.class);
            }
            catch (Exception ce) {
                System.out.println("Cannot connect to node: " + entry.getKey());
            }
        });

    }
}
