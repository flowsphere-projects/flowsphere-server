package com.flowsphere.server.business.controller;

import com.flowsphere.server.business.request.ConsumerRequest;
import com.flowsphere.server.business.service.ConsumerService;
import com.flowsphere.server.idempotent.IdempotentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private IdempotentService idempotentService;

    @Value("${flowsphere.server.consumer.idempotentTimeout:10}")
    private int idempotentTimeout;

    @PostMapping("/save")
    public ResponseEntity save(@RequestBody ConsumerRequest request) {

        try {
            idempotentService.idempotent(request.getApplicationName(), "", idempotentTimeout, TimeUnit.SECONDS);
            consumerService.save(request);
        } finally {
            idempotentService.delIdempotent();
        }
        return ResponseEntity.ok().build();
    }

}
