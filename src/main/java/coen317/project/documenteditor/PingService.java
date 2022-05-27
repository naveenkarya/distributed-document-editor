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
    NodesInfo nodesInfo;

    @Autowired
    LeaderElectionService leaderElectionService;

    @Scheduled(fixedRate = 1000)
    public void sendPingTo() {
        if (!nodesInfo.isLeader()) {
            try {
                String url = UriComponentsBuilder.newInstance()
                        .scheme("http").host(nodesInfo.getNodeMap().get(nodesInfo.getLeader()))
                        .path(PING_PATH).buildAndExpand(nodesInfo.getSelf()).toUriString();
                log.info("Leader node: {}", nodesInfo.getLeader());
                restTemplate.getForObject(url, Void.class);
            } catch (Exception ce) {
                log.info("Cannot connect to leader node: {}", nodesInfo.getLeader());
                nodesInfo.removeLeader();
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
