package com.flowsphere.server.business.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProviderInstantRequest implements Serializable {

    private String providerName;

    private String ip;

}
