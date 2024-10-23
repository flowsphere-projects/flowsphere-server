package com.flowsphere.server.business.controller;

import com.flowsphere.server.business.entity.Consumer;
import com.flowsphere.server.business.entity.ConsumerInstant;
import com.flowsphere.server.business.request.ConsumerRequest;
import com.flowsphere.server.business.service.ConsumerService;
import com.flowsphere.server.idempotent.IdempotentService;
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

    @GetMapping("/findByProviderNameOrConsumerName")
    public ResponseEntity<Page<Consumer>> findByProviderNameOrConsumerName(String providerName, String consumerName, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(consumerService.findByProviderNameOrConsumerName(providerName, consumerName, pageable));
    }


    @GetMapping("/findByProviderNameAndUrlAndConsumerName")
    public ResponseEntity<Page<ConsumerInstant>> findByProviderNameAndUrlAndConsumerName(String providerName, String url, String consumerName, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(consumerService.findByProviderNameAndUrlAndConsumerName(providerName, url, consumerName, pageable));
    }

}
