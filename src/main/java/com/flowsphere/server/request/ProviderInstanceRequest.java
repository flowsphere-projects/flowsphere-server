package com.flowsphere.server.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProviderInstanceRequest implements Serializable {

    private String providerName;

    private String ip;

    private int status;

    private int port;

}
