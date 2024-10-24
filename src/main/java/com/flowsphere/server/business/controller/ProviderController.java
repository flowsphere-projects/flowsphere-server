package com.flowsphere.server.business.controller;

import com.flowsphere.server.business.entity.Provider;
import com.flowsphere.server.business.entity.ProviderFunction;
import com.flowsphere.server.business.entity.ProviderInstant;
import com.flowsphere.server.business.request.ProviderFunctionRequest;
import com.flowsphere.server.business.request.ProviderInstantRequest;
import com.flowsphere.server.business.service.ProviderService;
import com.flowsphere.server.idempotent.IdempotentService;
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
            for (ProviderFunctionRequest request : list) {
                providerService.registerInstantFunction(request);
            }
        } finally {
            idempotentService.delIdempotent();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/findByName")
    public ResponseEntity<Page<Provider>> findByName(String providerName, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(providerService.findByName(providerName, pageable));
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
