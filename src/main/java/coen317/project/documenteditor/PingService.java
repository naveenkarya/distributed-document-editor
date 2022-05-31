package coen317.project.documenteditor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static coen317.project.documenteditor.NodeController.PING_PATH;

@Service
@Slf4j
public class PingService {
    RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());

    @Autowired
    NodesConfig nodesConfig;

    @Autowired
    LeaderElectionService leaderElectionService;

    @Scheduled(fixedRate = 1000)
    public void sendPingTo() {
        if(nodesConfig.getSelf() == 0) return;
        log.info("Node {} is connected to {}, and leader is {}", nodesConfig.getSelf(), nodesConfig.getNodeMap().keySet(), nodesConfig.getLeader());
        if (!nodesConfig.isLeader()) {
            try {
                String url = UriComponentsBuilder.newInstance()
                        .scheme("http").host(nodesConfig.getNodeMap().get(nodesConfig.getLeader()))
                        .path(PING_PATH).buildAndExpand(nodesConfig.getSelf()).toUriString();
                restTemplate.getForObject(url, Void.class);
            } catch (Exception ce) {
                log.info("Cannot connect to leader node: {}", nodesConfig.getLeader());
                //nodesInfo.removeLeader();
                leaderElectionService.electNewLeader();
            }
        }
    }

    private SimpleClientHttpRequestFactory getClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(200);
        clientHttpRequestFactory.setReadTimeout(200);
        return clientHttpRequestFactory;
    }
}
