package com.flowsphere.server.controller;

import com.flowsphere.server.entity.Consumer;
import com.flowsphere.server.entity.ConsumerInstant;
import com.flowsphere.server.entity.ConsumerProvider;
import com.flowsphere.server.idempotent.IdempotentService;
import com.flowsphere.server.request.ConsumerRequest;
import com.flowsphere.server.service.ConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/findByProviderNameAndConsumerName")
    public ResponseEntity<Page<Consumer>> findByProviderNameAndConsumerName(String providerName, String consumerName, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(consumerService.findByProviderNameAndConsumerName(providerName, consumerName, pageable));
    }


    @GetMapping("/findByProviderNameAndUrlAndConsumerName")
    public ResponseEntity<Page<ConsumerInstant>> findByProviderNameAndUrlAndConsumerName(String providerName, String url, String consumerName, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(consumerService.findByProviderNameAndUrlAndConsumerName(providerName, url, consumerName, pageable));
    }


    @GetMapping("/findByConsumerIdOrProviderIp")
    public ResponseEntity<Page<ConsumerProvider>> findByConsumerIdOrProviderIp(int consumerId, String providerIp, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(consumerService.findByConsumerIdOrProviderIp(consumerId, providerIp, pageable));
    }

}
