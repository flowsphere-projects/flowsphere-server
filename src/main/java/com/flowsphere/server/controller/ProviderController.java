package com.flowsphere.server.controller;

import com.flowsphere.server.entity.ProviderFunction;
import com.flowsphere.server.entity.ProviderInstant;
import com.flowsphere.server.idempotent.IdempotentService;
import com.flowsphere.server.request.ProviderFunctionRequest;
import com.flowsphere.server.request.ProviderInstantRequest;
import com.flowsphere.server.response.ProviderResponse;
import com.flowsphere.server.service.ProviderService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/provider")
public class ProviderController {

    @Autowired
    private ProviderService providerService;

    @Autowired
    private IdempotentService idempotentService;

    @Value("${flowsphere.server.provider.registerInstantIdempotentTimeout:10}")
    private int registerInstantIdempotentTimeout;

    @PostMapping("/modifyProviderInstantRemoval")
    public ResponseEntity modifyProviderInstantRemoval(@RequestBody ProviderInstantRequest request) {
        providerService.modifyProviderInstantRemoval(request.getProviderIp(), request.getStatus());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/registerInstant")
    public ResponseEntity registerInstant(@RequestBody ProviderInstantRequest request) {
        try {
            idempotentService.idempotent(request.getProviderName(), "registerInstant", registerInstantIdempotentTimeout, TimeUnit.SECONDS);
            providerService.registerInstant(request);
        } finally {
            idempotentService.delIdempotent();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/registerInstantFunction")
    public ResponseEntity registerInstantFunction(@RequestBody List<ProviderFunctionRequest> list) {
        try {
            idempotentService.idempotent(list.get(0).getProviderName(), "registerInstantFunction", registerInstantIdempotentTimeout, TimeUnit.SECONDS);
            List<List<ProviderFunctionRequest>> partition = Lists.partition(list, 20);
            for (List<ProviderFunctionRequest> requestList : partition) {
                providerService.registerInstantFunction(requestList);
            }
        } finally {
            idempotentService.delIdempotent();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/findProviderResponseByNameAndStatus")
    public ResponseEntity<Page<ProviderResponse>> findProviderResponseByNameAndStatus(String providerName, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(providerService.findProviderResponseByNameAndStatus(providerName, 1, pageable));
    }


    @GetMapping("/findInstantByProviderIdAndIp")
    public ResponseEntity<Page<ProviderInstant>> findInstantByProviderIdAndIp(Integer providerId, String ip, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(providerService.findInstantByProviderIdAndIp(providerId, ip, pageable));
    }

    @GetMapping("/findFunctionByProviderIdAndUrl")
    public ResponseEntity<Page<ProviderFunction>> findFunctionByProviderIdAndUrl(Integer providerId, String url, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(providerService.findFunctionByProviderIdAndUrl(providerId, url, pageable));
    }


}
