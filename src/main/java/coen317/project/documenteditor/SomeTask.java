package coen317.project.documenteditor;

import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;
@Slf4j
public class SomeTask extends TimerTask {
    int node;
    public SomeTask(int node) {
        super();
        this.node = node;
    }
    @Override
    public void run() {
        log.info("Timer triggered");
    }

}
