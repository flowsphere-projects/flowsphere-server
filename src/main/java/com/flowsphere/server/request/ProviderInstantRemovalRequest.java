package com.flowsphere.server.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProviderInstantRemovalRequest implements Serializable {

    private String providerName;

    private String providerIp;

    private int status;

}
