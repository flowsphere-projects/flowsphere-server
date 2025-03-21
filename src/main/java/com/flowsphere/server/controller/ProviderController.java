package com.flowsphere.server.controller;

import com.flowsphere.server.entity.ProviderFunction;
import com.flowsphere.server.entity.ProviderInstance;
import com.flowsphere.server.enums.ProviderStatusEnum;
import com.flowsphere.server.idempotent.IdempotentService;
import com.flowsphere.server.request.InstanceOfflineRequest;
import com.flowsphere.server.request.ProviderFunctionRequest;
import com.flowsphere.server.request.ProviderInstanceRemovalRequest;
import com.flowsphere.server.request.ProviderInstanceRequest;
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

    @Value("${flowsphere.server.provider.registerInstanceIdempotentTimeout:10}")
    private int registerInstantIdempotentTimeout;

    @PostMapping("/offline")
    public ResponseEntity offline(@RequestBody InstanceOfflineRequest request) {
        providerService.offline(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/modifyProviderInstanceRemoval")
    public ResponseEntity modifyProviderInstanceRemoval(@RequestBody ProviderInstanceRemovalRequest request) {
        providerService.modifyProviderInstanceRemoval(request.getIp(), request.getPort(), request.getStatus());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/registerInstance")
    public ResponseEntity registerInstance(@RequestBody ProviderInstanceRequest request) {
        try {
            idempotentService.idempotent(request.getProviderName() + request.getIp() + request.getPort(), "registerInstance", registerInstantIdempotentTimeout, TimeUnit.SECONDS);
            providerService.registerInstance(request);
        } finally {
            idempotentService.delIdempotent();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/registerInstanceFunction")
    public ResponseEntity registerInstanceFunction(@RequestBody List<ProviderFunctionRequest> list) {
        try {
            idempotentService.idempotent(list.get(0).getProviderName(), "registerInstanceFunction", registerInstantIdempotentTimeout, TimeUnit.SECONDS);
            List<List<ProviderFunctionRequest>> partition = Lists.partition(list, 20);
            for (List<ProviderFunctionRequest> requestList : partition) {
                providerService.registerInstanceFunction(requestList);
            }
        } finally {
            idempotentService.delIdempotent();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/findProviderResponseByNameAndStatus")
    public ResponseEntity<Page<ProviderResponse>> findProviderResponseByNameAndStatus(String providerName, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(providerService.findProviderResponseByNameAndStatus(providerName, ProviderStatusEnum.NORMAL.getStatus(), pageable));
    }


    @GetMapping("/findInstanceByProviderIdAndIp")
    public ResponseEntity<Page<ProviderInstance>> findInstanceByProviderIdAndIp(Integer providerId, String ip, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(providerService.findInstanceByProviderIdAndIp(null, providerId, ip, pageable));
    }

    @GetMapping("/findFunctionByProviderIdAndUrl")
    public ResponseEntity<Page<ProviderFunction>> findFunctionByProviderIdAndUrl(Integer providerId, String url, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(providerService.findFunctionByProviderIdAndUrl(providerId, url, pageable));
    }


}
