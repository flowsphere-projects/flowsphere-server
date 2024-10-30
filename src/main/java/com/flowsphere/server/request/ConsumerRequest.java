package com.flowsphere.server.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class ConsumerRequest implements Serializable {

    private String applicationName;

    private Map<String, List<String>> dependOnInterfaceList;

}
