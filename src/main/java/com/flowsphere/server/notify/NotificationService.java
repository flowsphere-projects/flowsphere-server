package com.flowsphere.server.notify;

import com.flowsphere.server.entity.Cmd;
import com.flowsphere.server.enums.CmdStatusEnum;
import com.flowsphere.server.repository.CmdRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class NotificationService implements InitializingBean {

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    @Autowired
    private NotificationListener notificationListener;

    @Value("${timeBeforeCertainMinutes:5}")
    private int timeBeforeCertainMinutes;

    @Value("${notificationDelay:1}")
    private int notificationDelay;

    @Autowired
    private CmdRepository cmdRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
        SCHEDULER.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(timeBeforeCertainMinutes);
                    List<Cmd> cmdList = cmdRepository.findFirst500ByCreateTimeGreaterThanEqualAndStatusOrderByCreateTimeAsc(localDateTime, CmdStatusEnum.NOT_NOTIFY.getStatus());
                    notificationListener.handler(cmdList);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }, 0, notificationDelay, TimeUnit.SECONDS);
    }

}
