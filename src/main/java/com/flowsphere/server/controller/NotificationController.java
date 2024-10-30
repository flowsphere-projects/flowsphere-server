package com.flowsphere.server.controller;

import com.flowsphere.server.heartbeat.HeartbeatManager;
import com.flowsphere.server.request.NotificationRequest;
import com.flowsphere.server.response.NotificationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;


@RestController
@RequestMapping("/notification")
public class NotificationController {

    private static final ResponseEntity<NotificationResponse>
            NOT_MODIFIED_RESPONSE = new ResponseEntity<>(HttpStatus.NOT_MODIFIED);

    @Value("${flowsphere.server.notification.timeout:30000}")
    private long notificationTimeout;

    @Autowired
    private HeartbeatManager heartbeatManager;

    @PostMapping("/pollNotification")
    public DeferredResult<ResponseEntity<NotificationResponse>> pollNotification(@RequestBody NotificationRequest request) {
        DeferredResult deferredResult = new DeferredResult(notificationTimeout, NOT_MODIFIED_RESPONSE);
        NotificationResponse notificationResponse = new NotificationResponse();
        ResponseEntity<NotificationResponse> response = ResponseEntity.ok(notificationResponse);
        heartbeatManager.receive(request.getApplicationName(), request.getIp());
        deferredResult.setResult(response);
        return deferredResult;
    }

}
