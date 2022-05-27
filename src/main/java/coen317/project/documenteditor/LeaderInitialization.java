package coen317.project.documenteditor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Timer;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LeaderInitialization {

    @Autowired
    private NodesInfo nodesInfo;
    @Autowired
    FollowerStatusService followerStatusService;


    @EventListener(ApplicationReadyEvent.class)
    public void checkFollowers() {
        if(nodesInfo.isLeader()) {
            log.info("Leader {} is up. Checking for followers", nodesInfo.getSelf());
            Map<Integer, String> followers = nodesInfo.getNodeMap().entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
            for(Map.Entry<Integer, String> entry: followers.entrySet()) {
                if(followerStatusService.checkFollowerStatus(entry.getKey(), entry.getValue(), nodesInfo.getSelf())) {
                    Timer timer = new Timer();
                    RemoveFailedNode removeFailedNode = new RemoveFailedNode(entry.getKey(), nodesInfo);
                    nodesInfo.getTimerMap().put(entry.getKey(), timer);
                    timer.schedule(removeFailedNode, 2000);
                }
            }
        }
    }


}
