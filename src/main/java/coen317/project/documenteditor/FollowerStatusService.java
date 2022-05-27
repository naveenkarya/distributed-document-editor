package coen317.project.documenteditor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Timer;

import static coen317.project.documenteditor.NodeController.PING_PATH;

@Service
@Slf4j
public class FollowerStatusService {
    RestTemplate restTemplate = new RestTemplate();

    @Autowired
    RetryTemplate retryTemplate;

    public boolean checkFollowerStatus(int follower, String host, int leader) {
        log.info("follower {}. Thread: {}", follower, Thread.currentThread().getName());
        return retryTemplate.execute(new RetryCallback<Boolean, RuntimeException>() {
            @Override
            public Boolean doWithRetry(RetryContext retryContext) throws RuntimeException {
                log.info("Retry count {} for follower {}", retryContext.getRetryCount(), follower);
                log.info("Checking if follower {} is up.", follower);
                String url = UriComponentsBuilder.newInstance()
                        .scheme("http").host(host)
                        .path(PING_PATH).buildAndExpand(leader).toUriString();
                log.info("Checking is follower {} is up and running.", follower);
                restTemplate.getForEntity(url, Void.class);
                return true;
            }
        }, new RecoveryCallback<Boolean>() {
            @Override
            public Boolean recover(RetryContext retryContext) throws RuntimeException {
                return false;
            }
        });

    }
}
