package coen317.project.documenteditor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

import static coen317.project.documenteditor.DocumentController.DOCUMENT_GET_ALL_PATH;

@Service
@Slf4j
public class NodeInitialization {

    @Autowired
    private NodesConfig nodesConfig;
    private static final String LB_ADD_NODE_PATH = "/node/add/{port}";

    RestTemplate restTemplate = new RestTemplate();
    @Autowired
    DocumentRepository documentRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeNode() {
        if (nodesConfig.getSelf() == 0) {
            updateExistingNodes();
            addSelf();
        }

        if (!nodesConfig.isLeader()) {
            try {
                String[] hostAndPort = nodesConfig.getNodeMap().get(nodesConfig.getLeader()).split(":");
                String url = UriComponentsBuilder.newInstance()
                        .scheme("http").host(hostAndPort[0]).port(hostAndPort[1])
                        .path(DOCUMENT_GET_ALL_PATH).toUriString();
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                List<WordDocument> documentList1 = objectMapper.convertValue(restTemplate.getForObject(url, List.class), new TypeReference<List<WordDocument>>() {
                });
                for (int i = 0; i < documentList1.size(); i++) {
                    WordDocument replicateDocument = documentList1.get(i);
                    documentRepository.save(replicateDocument);
                }
                log.info("Replicating all documents ");
            } catch (Exception ce) {
                log.info("Unable to copy: " + ce.getMessage());
            }
        }
    }

    private void updateExistingNodes() {
        String[] lbHostPort = nodesConfig.getLoadBalancer().split(":");
        String url = UriComponentsBuilder.newInstance()
                .scheme("http").host(lbHostPort[0]).port(lbHostPort[1])
                .path("/node/map").toUriString();
        NodeInfoResponse nodeInfoResponse = restTemplate.getForEntity(url, NodeInfoResponse.class).getBody();
        nodesConfig.setLeader(nodeInfoResponse.getLeader());
        nodesConfig.setNodeMap(nodeInfoResponse.getNodeMap().entrySet().stream()
                .collect(Collectors.toConcurrentMap(e -> e.getKey(), e -> "localhost:" + e.getValue())));
    }

    private void addSelf() {
        String[] lbHostPort = nodesConfig.getLoadBalancer().split(":");
        String url = UriComponentsBuilder.newInstance()
                .scheme("http").host(lbHostPort[0]).port(lbHostPort[1])
                .path(LB_ADD_NODE_PATH).buildAndExpand(nodesConfig.getSelfPort()).toUriString();
        ResponseEntity<Integer> selfNode = restTemplate.getForEntity(url, Integer.class);
        nodesConfig.setSelf(selfNode.getBody());
    }

}
