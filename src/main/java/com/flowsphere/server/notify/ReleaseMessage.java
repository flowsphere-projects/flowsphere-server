package com.flowsphere.server.notify;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ReleaseMessage {

    private String cmd;

    private String applicationName;

    private String extendData;

}
