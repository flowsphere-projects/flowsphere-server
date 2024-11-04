package com.flowsphere.server.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProviderResponse implements Serializable {

    private int id;

    private String name;

    private int status;

    private int onlineNumber;

}
