package com.flowsphere.server.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class InstanceOfflineRequest implements Serializable {

    private String applicationName;

    private String ip;

    private int port;

}
