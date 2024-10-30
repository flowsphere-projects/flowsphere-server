package com.flowsphere.server.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class NotificationRequest implements Serializable {

    private String applicationName;

    private String ip;

}
