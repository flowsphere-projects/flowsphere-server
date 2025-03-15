package com.flowsphere.server.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProviderInstanceRemovalRequest implements Serializable {

    private String providerName;

    private String ip;

    private int port;

    private int status;

}
